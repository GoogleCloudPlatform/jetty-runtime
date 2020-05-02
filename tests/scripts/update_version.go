// update_version updates the Jetty version referenced in pom.xml
//
// Usage:
//   go run update_version.go <new version>
//
// Example:
//   go run update_version.go 9.4.17.v20190418
package main

import (
	"bufio"
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"os"
	"regexp"
	"strings"

	"github.com/blang/semver"
	"github.com/pkg/errors"
)

const (
	// XMLPath is the path containing the active version of Jetty.
	XMLPath = "../pom.xml"
	// SupportedMajorRelease is the current major release this project supports.
	SupportedMajorRelease = 9
)

var (
	// errVersion is an empty version returned when there is an error
	errVersion = semver.MustParse("0.0.0")
	// minorRe is how we find the minor version in pom.xml
	minorRe = regexp.MustCompile(`(.*<jetty9.minor.version>)(\d+)(</jetty9.minor.version>)`)
	// patchRe is how we find the patch version in pom.xml
	patchRe = regexp.MustCompile(`(.*<jetty9.dot.version>)(\d+)(</jetty9.dot.version>)`)
	// buildRe is how we find the build version in pom.xml
	buildRe = regexp.MustCompile(`(.*<jetty9.version>9.\${jetty9.minor.version}.\${jetty9.dot.version}.)(v\d+)(</jetty9.version>)`)
)

// Check returns an error if the requested version is not active
func Check(want semver.Version) error {
	got, err := currentVersion(XMLPath)
	if err != nil {
		return errors.Wrap(err, "currentVersion")
	}
	// semver.Version cannot be directly compared if Build/PR metadata are in use.
	if got.String() != want.String() {
		return fmt.Errorf("Current version is %s, not %s", got, want)
	}
	return nil
}

// Update idempotently updates the jetty version in XMLPath
func Update(want semver.Version) error {
	if err := Check(want); err == nil {
		return nil
		// Current version is active -- nothing to do!
	}
	if want.Major != SupportedMajorRelease {
		return fmt.Errorf("Updating to a release other than %d.x is unsupported", SupportedMajorRelease)
	}

	f, err := os.Open(XMLPath)
	if err != nil {
		return err
	}
	defer f.Close()

	tf, err := ioutil.TempFile(".", "pom.xml.update")
	if err != nil {
		return err
	}

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		line := scanner.Text()
		if match := minorRe.FindStringSubmatch(line); match != nil {
			log.Printf("Rewriting minor version to %d in %s", want.Minor, line)
			io.WriteString(tf, fmt.Sprintf("%s%d%s\n", match[1], want.Minor, match[3]))
			continue
		}
		if match := patchRe.FindStringSubmatch(line); match != nil {
			log.Printf("Rewriting patch version to %d in %s", want.Patch, line)
			io.WriteString(tf, fmt.Sprintf("%s%d%s\n", match[1], want.Patch, match[3]))
			continue
		}
		if match := buildRe.FindStringSubmatch(line); match != nil {
			log.Printf("Rewriting build to %s in %s", want.Build[0], line)
			io.WriteString(tf, fmt.Sprintf("%s%s%s\n", match[1], want.Build[0], match[3]))
			continue
		}
		io.WriteString(tf, line+"\n")
	}
	if err := scanner.Err(); err != nil {
		return err
	}

	tf.Close()

	generated, err := currentVersion(tf.Name())
	if err != nil {
		return errors.Wrap(err, "currentVersion")
	}
	if generated.String() != want.String() {
		return fmt.Errorf("Update failure: generated %s instead of %s", generated, want)
	}
	return os.Rename(tf.Name(), XMLPath)
}

// currentVersion parses the jetty version in pom.xml -- assuming Jetty 9.x
func currentVersion(path string) (semver.Version, error) {
	f, err := os.Open(path)
	if err != nil {
		return errVersion, err
	}
	defer f.Close()

	minor := ""
	patch := ""
	build := ""

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		line := scanner.Text()
		if match := minorRe.FindStringSubmatch(line); match != nil {
			minor = match[2]
			log.Printf("Found minor version %s in %s", minor, path)
			continue
		}
		if match := patchRe.FindStringSubmatch(line); match != nil {
			patch = match[2]
			log.Printf("Found patch version %s in %s", patch, path)
			continue
		}
		if match := buildRe.FindStringSubmatch(line); match != nil {
			build = match[2]
			log.Printf("Found release date %s in %s", build, path)
			continue
		}
	}

	if err := scanner.Err(); err != nil {
		return errVersion, err
	}

	if minor == "" {
		return errVersion, fmt.Errorf("No minor version found in %s", path)
	}
	if patch == "" {
		return errVersion, fmt.Errorf("No patch version found in %s", path)
	}
	if build == "" {
		return errVersion, fmt.Errorf("No build found in %s", path)
	}

	found := fmt.Sprintf("%d.%s.%s.%s", SupportedMajorRelease, minor, patch, build)
	log.Printf("Found %s version in %s", found, path)
	return parseVersion(found)
}

// parseVersion converts x.y.z.v2359 into semver.
func parseVersion(version string) (semver.Version, error) {
	parts := strings.Split(version, ".v")
	if len(parts) != 2 {
		return errVersion, fmt.Errorf("version does not contain build info (.v)")
	}
	v, err := semver.Parse(parts[0])
	if err != nil {
		return errVersion, errors.Wrap(err, fmt.Sprintf("semver.Parse(%s)", version))
	}
	v.Build = append(v.Build, fmt.Sprintf("v%s", parts[1]))
	return v, nil
}

func main() {
	if len(os.Args) == 1 {
		fmt.Println("need version argument")
		os.Exit(1)
	}
	version := os.Args[1]
	v, err := parseVersion(version)
	if err != nil {
		fmt.Printf("Unable to parse %s: %v\n", version, err)
		os.Exit(3)
	}

	err = Check(v)
	if err == nil {
		fmt.Println("Pre-check passes! There is no work to be done.\n")
		os.Exit(0)
	}
	fmt.Printf("Pre-check failed: %v\n", err)

	err = Update(v)
	if err != nil {
		fmt.Printf("Update failed: %v\n", err)
		os.Exit(1)
	}

	err = Check(v)
	if err != nil {
		fmt.Printf("Post-check failed: %v\n", err)
		os.Exit(2)
	}

	fmt.Printf("jetty version in %s is now %s\n", XMLPath, version)
}

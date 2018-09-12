# BenchmarkAndroid
An initial Benchmark for Automatic Software Repair with 13 single-bugs from 5 Open Source Android projects.

Contacts:

Larissa S de Azevedo - lahazevedo07@gmail.com

Celso G Camilo-Junior - celsocamilo@gmail.com

## Benchmark's Composition

The Benchmark has 13 bugs from 5 Open Source Android apps: Wikipedia Android, K-9 Mail, Kore, Poet Assistant e Loop Habit Tracker. Each bug contains:

1. ***PRJ_Bug.zip***: the buggy version.
2. ***PRJ_Bug_appName_version.zip***: A version of PRJ_Bug modified to run in [Astor4Android](https://github.com/kayquesousa/astor4android).
3. ***PRJ_Fix.zip***: the fixed version.
4. ***Files_previous***:  the files of the buggy version that were modified by the PRJ_Fix commit.
5. ***Files_uptaded***: the files modified in the fixed version commit.
6. ***Test_Suite***:
	- ***Test_Cases***: all files with test cases (test suite) for the fixed version.
	- ***PRJ_Bug***: reports from test suite execution over the buggy version.
	- ***PRJ_Fix***: reports from the test suite execution over fixed version.
7. **changes.txt**: changes made in PRJ_Bug version to enable its execution in [Astor4Android](https://github.com/kayquesousa/astor4android), resulting in PRJ_Bug_appName_version version.
8. **diff_lines.txt**: the diff between the buggy and fixed version.
9. **diff_test.txt**: test cases that changed their results in PRJ_Fix in comparison to PRJ_Bug.
10. **file_diff.txt**: a list of files modified by PRJ_Fix.
11. **appName_version.sh**: script to run PRJ_Bug_appName_version in Astor4Android. It is necessary to put the absolute path of PRJ_Bug_appName_version in the script where “$LOCATION” is written.

[Astor4Android](https://github.com/kayquesousa/astor4android) is an adaptation of the original [Astor](https://github.com/SpoonLabs/astor) to work with Android applications. Please, access its original repository at [Astor4Android](https://github.com/kayquesousa/astor4android).










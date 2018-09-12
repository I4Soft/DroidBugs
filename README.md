# BenchmarkAndroid
An initial Benchmark for Automatic Software Repair with 13 single-bugs from 5 Open Source Android projects.

Contacts:

Larissa S de Azevedo - lahazevedo07@gmail.com

Celso G Camilo-Junior - celsocamilo@gmail.com

## Benchmark's Composition

The Benchmark has 13 bugs from 5 Open Source Android apps: Wikipedia Android, K-9 Mail, Kore, Poet Assistant e Loop Habit Tracker. Each bug contains:

1. ***Files_previous:*** The version in PRJ_Bug of the files modified by the PRJ_Fix commit. 
2. ***Files_uptaded:*** The version in PRJ_Fix of the files modified by the PRJ_Fix commit.
3. ***Test_Suite:***
	- ***Test_Cases:*** All files with test cases in PRJ_Fix. 
	- ***PRJ_Bug:*** Reports from the testsuite execution in PRJ_Bug. 
	- ***PRJ_Fix:*** Reports from the testsuite execution in PRJ_Fix. 
4. **PRJ_Bug.zip:** project’s version that has a bug.
5. **PRJ_Bug_appName_version.zip:** version of PRJ_Bug modified to run in [Astor4Android](https://github.com/kayquesousa/astor4android).
6. **PRJ_Fix.zip:** project’s version where the bug was fixed by the developer. 
7. **changes.txt**: changes made to PRJ_Bug version to enable its use in Astor4Android, resulting in *PRJ_Bug_appName_version* version.
8. **diff_lines.txt:** lines that were modified by PRJ_Fix. 
9. **diff_test.txt:** test cases that changed their results in PRJ_Fix in comparison to PRJ_Bug.
10. **file_diff.txt:** files that were modified by PRJ_Fix.
11. **appName_version.sh:** script to run *PRJ_Bug_appName_version* in [Astor4Android](https://github.com/kayquesousa/astor4android). It is necessary to put the absolute path of *PRJ_Bug_appName_version* in the script  where “$LOCATION” is written. 













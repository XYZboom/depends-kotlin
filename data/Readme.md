## This folder contains dataset of the paper: Depends-Kotlin: A Cross-language Kotlin Dependency Extractor.

Folder **project-source-code** contains the source code of 3 projects analyzed by Depends-Kotlin in thd paper.
Use `git submodule update --init --recursive` to fetch the source code.

Folder **depends-kotlin-output** contains the analysis results of the 3 projects. In each project's subfolder, it includes 3 json-formatted files. 
1. file.json shows dependency relations at file level.
2. method.json shows dependency relations at method level.
3. structure.json shows dependency relations among entities at all levels, including package, file, class, method, and variables.

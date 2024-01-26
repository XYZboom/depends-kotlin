## Introduction

**This is a kotlin(jvm) module of project [Depends](https://github.com/multilang-depends/depends)**

Since Google introduced Kotlin as an official programming language for developing Android apps in 2017, Kotlin has gained widespread
adoption in Android development. However, compared to Java, there is limited support for Kotlin code dependency analysis, which is the foundation to software analysis and techniques. To bridge this gap, we introduced Depends-Kotlin to extract entities and their dependencies in Kotlin source code. Not only does Depends-Kotlin support extracting entities’ dependencies in Kotlin code, but it also can extract invocation dependency between Kotlin and Java.

## How to Use

Depends-Kotlin can be built and run by following 3 steps. 

- [x] **Step 1. Clone repository** 

Our repository is hosted on GitHub, you can clone the source code with the following command: 

```bash
git clone https://github.com/XYZboom/depends-kotlin.git 
```

- [x] **Step 2. Compile project** 

Before compiling, make sure you have the Java 17 runtime ready. After that, run the following command at the root of your project to compile it: 

- on windows 

```powershell
gradlew.bat build 
```

- on linux 

```bash
gradlew build 
```

Meanwhile, the -x test option can be used to skip testing. At this point, Gradle Wapper will pull the necessary dependencies and compile them.

- [x] **Step 3. Generate Runnable Jar**

After successful compilation, you can use Gradle’s shadow plugin to package it as an executable jar using the following command: 

- on windows 

```powershell
gradlew.bat shadowJar 
```

- on linux 

```bash
gradlew shadowJar 
```

You will see the following output and find an executable jar package with the suffix **. all.jar* in the *projectRoot/build/libs* folder.

## Run Tool

Depends-Kotlin is based on the JVM platform and requires at least Java 17. The input of Depends-Kotlin is the source folder path of a particular project. The full command line arguments are as follows: 

    Usage: depends [-hms] [--auto-include] [-d=<dir>] [-g=<granularity>]
                   [-p=<namePathPattern>] [-f=<format>[,<format>...]]...
                   [-i=<includes>[,<includes>...]]... <lang> <src> <output>
          <lang>                 The language of project files
          <src>                  The directory to be analyzed
          <output>               The output file name
          --auto-include         auto include all paths under the source path 
      -i, --includes             The files of searching path
      -d, --dir                  The output directory
      -f, --format               The output format
      -g, --granularity=<granularity>
                                 Granularity of dependency.  
      -h, --help                 Display this help and exit
      -s, --strip-leading-path   Strip the leading path.
      
      -m, --n-map-files          Output DV8 dependency map file.
      -p, --namepattern=<namePathPattern>
                                 The name path separators.[default(/),dot(.)

For example, if we need to analyze a Kotlin(-Java) project “sqlex”, we can execute the command as follows:

```bash
 java -jar depends-kotlin.jar kotlin ./sqlex result -d ./out
```


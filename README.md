## Gradle LaTeX plugin

### Why a Gradle LaTeX plugin?

Truth be told: I wrote this little plugin to improve my poor Groovy skills and also to get into the development of Gradle plugins. Having said that, this plugin doesn't have the aim to cover all possible ways of compiling LaTeX code. It rather provides structure for LaTeX projects when `pdflatex` and `biber` is used. This is because I use `pdflatex` and `biber` to compile my own stuff for university and I was sick of having no predefined structure of my projects.

### How to get the plugin

You can get the plugin by cloning the repository and executing `gradle install` from the terminal. This will install the plugin into your local Maven repository. Gradle 2.0 or higher is recommended. Perhaps I'll provide a solution with the Gradle wrapper later on.

### How to use the plugin

You apply the plugin to your Gradle build by adding the following lines to the top of your build file:

```groovy
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath 'edu.hm.cs.goetz1.gradle.latex:gradle-latex:0.1-SNAPSHOT'
    }
}

apply plugin: 'latex'

```

This will provide 4 tasks to your build:

+ A task `pdflatexPreBuild`: 

 This task initially compiles your document with `pdflatex` to produce the "standard" outputs like the table of contents file (`.toc`) or the bibliography information (`.bcf`) used by `biber` later on.
+ A task `biber` (depends on `pdflatexPreBuild`):
 
 This task actually runs biber on your pre build. It produces the biblatex auxiliary file (`.bbl`) which is needed to produce the final pdf containing your bibliography references.
+ A task `pdflatex` (depends on `biber`): 
 
 This task produces your final pdf.
+ A task `cleanPdflatex`: 
 
 This task cleans the output directory of your build, which is `build/latex` by default.


#### Standard directory structure of your LaTeX build

As explained earlier, the plugin provides structure for LaTeX projects and therefore expects and uses a specific directory pattern:

```
{project-name}/
  ├─ build/
  │    ├─ latex/
  ├─ src/
  │    ├─ {project-name}.tex
  │    ├─ any-subdirectories/
  │    └─ ...
  └─ build.gradle
 
```

As you can see, the plugin expects a "main" tex file in the source directory, which has exactly the same name as the project. The name of the project is -- by the convention of Gradle -- the name of the root directory of your project, in this case `{project-name}`. If you want your main tex file to be named differently, you can achieve this by specifying it this way:

```groovy
...
apply plugin: 'latex'

latex {
    mainFilename = "somethingelse"
}
```

You can also explicitly specify the directory of your source files, for example:

```groovy
latex {
    sourceDir = "src/docs/latex/"
}
```

The directory of your output files can also be configured:

```groovy
latex {
    outputDir = "bin/"
}
```

### What you can do with the plugin

The plugin supports incremental builds, which means that tasks are skipped when neither the input nor the output files have changed. When `pdflatex` gets executed, `pdflatexPreBuild` and `biber` also get executed when they're not up-to-date. Furthermore, `biber` is skipped when there are no cites in your document.

### What you can _not_ do with the plugin so far

At the moment, only biblatex in combination with biber is supported. You can not compile LaTeX documents using bibtex with this plugin. The plugin is also only tested with TeXLive on Linux (Ubuntu). I try to test it on other platforms in the future.

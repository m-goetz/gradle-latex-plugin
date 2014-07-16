package edu.hm.cs.goetz1.gradle.latex;

import org.junit.Test
import static org.junit.Assert.*

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Exec

public class LatexPluginTest {

    @Test
    public void latexPluginAddsPdflatexPreBuildTask() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'latex'

        assertTrue(project.tasks.pdflatexPreBuild instanceof Exec)
    }
    
    @Test
    public void latexPluginAddsBiberTask() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'latex'

        assertTrue(project.tasks.biber instanceof Exec)
        assertTrue(taskDependsOnOther(project.tasks.biber, project.tasks.pdflatexPreBuild))
    }
    
    @Test
    public void latexPluginAddsPdflatexTask() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'latex'

        assertTrue(project.tasks.pdflatex instanceof Exec)
        assertTrue(taskDependsOnOther(project.tasks.pdflatex, project.tasks.biber))
    }
    
    private boolean taskDependsOnOther(Task dependend, Task dependency) {
        return dependend.taskDependencies.getDependencies(dependend).contains(dependency)
    }
}

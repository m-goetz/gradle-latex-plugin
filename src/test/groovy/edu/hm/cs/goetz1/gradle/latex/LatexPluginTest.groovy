/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.hm.cs.goetz1.gradle.latex;

import org.junit.Test
import static org.junit.Assert.*

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Exec

/**
 * Tests the LaTeX plugin.
 *
 * @author: Maximilian Götz
 *
 */
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

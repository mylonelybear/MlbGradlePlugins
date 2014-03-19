package com.mylonelybear.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

import org.gradle.api.tasks.wrapper.Wrapper

class MyLonelyBearPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.group = 'com.mylonelybear'

        project.apply(plugin: 'ivy-publish')

        project.task('wrapper', type: Wrapper) << {
            gradleVersion = 1.11
        }

        project.repositories {
            ivy {
                name "ivyLocal"
                url "${System.getProperty('user.home')}/.ivy/repo"
            }
        }

        project.publishing {
            repositories {
                add project.repositories.ivyLocal
            }
        }
    }
}


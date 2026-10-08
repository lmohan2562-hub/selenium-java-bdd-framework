// Declarative pipeline. Requires JDK 17 ('jdk17') and Maven ('maven3') tools, and Chrome on the agent,
// or a Selenium Grid URL (e.g. the one from docker-compose.yml) reachable from the agent.
pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    parameters {
        choice(name: 'BROWSER', choices: ['chrome', 'firefox', 'edge'], description: 'Browser')
        string(name: 'TAGS', defaultValue: 'not @wip', description: 'Cucumber tag expression, e.g. @smoke')
        string(name: 'GRID_URL', defaultValue: '', description: 'Optional Selenium Grid URL (blank = local headless browser)')
        string(name: 'APP_HOST', defaultValue: '', description: 'Host name Grid nodes use to reach this agent (required with GRID_URL)')
        string(name: 'THREADS', defaultValue: '2', description: 'Parallel scenarios')
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    stages {
        stage('UI tests') {
            steps {
                script {
                    def remote = params.GRID_URL?.trim()
                            ? "-Dremote.url=${params.GRID_URL} -Dapp.bind=0.0.0.0 -Dapp.host=${params.APP_HOST}"
                            : ''
                    sh """mvn -B verify -Dbrowser=${params.BROWSER} -Dheadless=true -Dthreads=${params.THREADS} \
                        "-Dcucumber.filter.tags=${params.TAGS}" ${remote}"""
                }
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/**/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'target/cucumber-reports/**, target/screenshots/**', allowEmptyArchive: true
        }
    }
}

//declarative pipeline style
//Jenkinsfile

def checkOs(){
    if (isUnix()) {
        def uname = sh script: 'uname', returnStdout: true
        if (uname.startsWith("Darwin")) {
            return "Macos"
        }
        // Optionally add 'else if' for other Unix OS  
        else {
            return "Linux"
        }
    }
    else {
        return "Windows"
    }
}

pipeline{
    //any is good for when you have clusters, running on the next available agent
    agent any

    environment{
        VERACODE_APP_NAME = "Verademo"
        DBG = true
        HOST_OS = checkOs()
        //checkout([$class: 'GitSCM', branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/veracode/verademo']]])
    }//end env

    // tools{
    //     maven "Maven-3"
    // }

    stages{
        stage("Enviornment Set Up"){
            steps{
                echo '============================ Determine the operating system ============================'
                echo 'The pipeline is being built in: '
                echo checkOs()
                script{
                    env.HOST_OS = checkOs()
                }//end script
                git branch: 'main', url: 'https://github.com/veracode/verademo'

                // echo '============================ Downloading Pipeline Files =================================='
                // script{
                //     if(isUnix() == false){
                //         bat 'curl https://downloads.veracode.com/securityscan/pipeline-scan-LATEST.zip -o pipeline-scan.zip'
                //         powershell 'Set-ExecutionPolicy AllSigned -Scope Process -Force'
                        
                //     }
                //     else{
                //         sh 'curl https://downloads.veracode.com/securityscan/pipeline-scan-LATEST.zip -o pipeline-scan.zip'
                        
                //     }
                // }
                //powershell 'Expand-Archive -Force -Path pipeline-scan.zip -DestinationPath pipeline-scanner '
                //echo '============================= Unzipping Files ============================================'
                //unzip test: true, zipFile: 'pipeline-scan.zip'
                

                echo '============================ Finishing environment checkout stage ============================'
                
            }//end steps
        }//end stage
      
        stage("Build"){
            steps{
                echo '============================ Building the application ============================'
                script{
                    if(isUnix() == false){
                        //Moves into app directory
                        dir("app"){
                            bat 'dir'
                            bat 'mvn clean verify'
                            //bat 'mvn package'
                        }//end dir
                        echo '===================== Checking directorty after build ====================='
                        powershell 'ls'
                    }//end if
                    else{
                        dir("app"){
                            sh 'ls'
                            sh 'mvn clean verify'
                            //sh 'mvn package'
                        }//end dir
                        
                        echo '===================== Checking directorty after build ====================='
                        sh 'ls'
                        sh 'pwd'
                    }//end else
                }//end script
              echo '============================ Ending the build stage ============================'
            }//end steps
        }//end stage
      
        stage("Upload and Scan"){

            steps{
                echo '============================ Testing the application ============================'
                withCredentials([usernamePassword(credentialsId: 'veracode_login', passwordVariable: 'veracode_api_key', usernameVariable: 'veracode_api_id')]) {
                    veracode applicationName: 'Verademo', criticality: 'VeryHigh', debug: true, deleteIncompleteScan: true, fileNamePattern: '', replacementPattern: '', sandboxName: '', scanExcludesPattern: '', scanIncludesPattern: '', scanName: '$buildnumber', teams: '', timeout: 60, uploadIncludesPattern: '**/**.war', vid: "${veracode_api_id}", vkey: "${veracode_api_key}", waitForScan: true
                }//end withCredentials

            }//end steps
        }//end stage
      
        // stage("pipeline scan"){

        //     steps{
        //         echo '============================ Scanning the application ============================'
        //         script{
        //             if(isUnix() == false){ 
        //                 //windows
        //                 echo '---------------------------- Pipeline scan starting - Windows based -----------------------------'
        //                 withCredentials([usernamePassword(credentialsId: 'veracode_login', passwordVariable: 'veracode_api_key', usernameVariable: 'veracode_api_id')]) {
        //                     powershell '''java -jar pipeline-scanner/pipeline-scan.jar -f app/target/verademo.war --veracode_api_id "$env:veracode_api_id" -vkey "$env:veracode_api_key" -f app/target/verademo.war  -id true --fail_on_severity="Very High, High" --fail_on_cwe="80" --fail_on_severity="Very High, High" ''' 
        //                 }//end withCredentials
        //             }//end if
        //             else{
        //                 //linux
        //                 echo '---------------------------- Pipeline scan starting - Unix based -----------------------------'
        //                 withCredentials([usernamePassword(credentialsId: 'veracode_login', passwordVariable: 'veracode_api_key', usernameVariable: 'veracode_api_id')]) {
        //                     sh '''java -jar pipeline-scanner/pipeline-scan.jar -f app/target/verademo.war --veracode_api_id "$env:veracode_api_id" -vkey "$env:veracode_api_key" -f app/target/verademo.war  -id true --fail_on_severity="Very High, High" --fail_on_cwe="80" --fail_on_severity="Very High, High" ''' 
        //                 }//end withCredentials
        //             }//end else
        //         }//end script
        //         //recieves the error that the -version is required for the selected action ( parsing error )
        //     }//end steps
        // }//end stage
      
        stage("SCA scan"){
            steps{
                script{
                    dir("app"){
                        withCredentials([string(credentialsId: 'SRCCLR_API_TOKEN', variable: 'SRCCLR_API_TOKEN')]) {
                            if(isUnix() == true){
                                sh 'curl -sSL https://download.sourceclear.com/ci.sh | sh'
                                //sh "curl -sSL https://download.sourceclear.com/ci.sh | DEBUG=1 sh -s -- scan --no-upload"
                            }//end if
                            else{
                                powershell 'ls'
                                powershell "iex ((New-Object System.Net.WebClient).DownloadString('https://download.srcclr.com/ci.ps1')); srcclr scan "
                            }//end else
                        }//end withCredentials
                    }//end dir
               
                }//end script
            }//end steps
        }//end stage
        
    }
    post{
        always{
            //things that are always run
          
            
            echo '============================ finished pipeline ============================'
        }//end always
        failure{
            //things that run on failure
            echo '============================  failed  ============================'
        }//end failure
        success{
            //things that run on success
            echo '============================  Success  ============================'
        }//end success
    }//end post
}//end pipeline

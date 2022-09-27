//declarative pipeline style


// main
pipeline{

    //any is good for when you have clusters, running on the next available agent
    agent any

    //env
    environment{
        def VERACODE_APP_NAME = "Verademo"
        def DBG = true
        //
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Stages
    //stages of the pipeline 
    stages{


        
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Setting Up the Enviornment
        //a singular stage within the pipeline, defined actions in steps
        // Precondition: N/A
        // Postcondition: The repo will be checked out of github and the pipeline script will be downloaded and unzipped.
        stage("Setting up Enviornment"){

            steps{

                //check out the git SCM repo 
                checkout([$class: 'GitSCM', branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/veracode/verademo']]])
                
                //A groovy script to be run within the pipeline
                script{//running a pipeline groovy script
                    
                    //Condtional: Checks to see which operating system is running
                    if(isUnix() == false){//if running a unix based ( ie, linux vs Windows )

                        bat 'curl https://downloads.veracode.com/securityscan/pipeline-scan-LATEST.zip -o pipeline-scan.zip'
                        powershell 'Set-ExecutionPolicy AllSigned -Scope Process -Force'
                        //powershell 'Expand-Archive -Force -Path pipeline-scan.zip -DestinationPath pipeline-scan '
                        powershell 'ls'
                    }//end if
                    else{
                        sh 'curl https://downloads.veracode.com/securityscan/pipeline-scan-LATEST.zip -o pipeline-scan.zip'
                        //sh 'unzip pipeline-scan.zip '
                        sh 'ls'
                    }//end else
                    //unzip the file if not done so within each individual conditional
                    //unzip zipFile: 'pipeline-scan.zip'

                }//end script
                //unzip the file using powershell example
                //powershell 'Expand-Archive -Force -Path pipeline-scan.zip -DestinationPath pipeline-scanner '
                echo '============================= Unzipping Files ============================================'
                //unzipping the file using jenkins unzip command, testing the archive as well
                unzip test: true, zipFile: 'pipeline-scan.zip'
                println("Current Working Directory: " + pwd())


            }//end steps
        }//end stage
        
        
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Build
        //start of the build stage
        // Precondition: The proper build instructions for the application in question should be present in the stage below ( ie, in this case maven )
        // Postcondition: The application will be built with the specified build instructions
        stage("Stage 1 - Build "){

            //execution
            steps{
                
                println("Stage 1 - Build");
                println("Current Working Directory: " + pwd());
                
                //moving into the app directory
                dir("app"){

                    println("Current Working Directory: " + pwd());
                    
                    //groovy script
                    script{

                        //Conditional: determines OS
                        if(isUnix() == false){
                            //Moves into app directory
                            
                            //checks the directory
                            bat 'dir'
                            //build using maven and clean build and then verify
                            bat 'mvn clean package verify'
                            //bat 'mvn package'

                            echo '===================== Checking directorty after build ====================='
                            
                            powershell 'ls'
                        }//end if
                        else{
                            
                            //list out the current files and directories
                            sh 'ls'
                            //build the package using maven and clean build and then verify
                            sh 'mvn clean package verify'
                            //sh 'mvn package'
                        
                            
                            echo '===================== Checking directorty after build ====================='
                            
                            sh 'ls'
                            println("Current Working Directory: " + pwd());
                        }//end else
                    }//end script
                }//end dir

            }//end steps

        }//end stage

        
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Test
        // The Testing stage of the pipeline, currently a stub
        // Precondition: The application should be built prior to this stage and the proper test files made availble
        // Postcondition: A result of the tests run
        stage("Stage 2 - Test"){
            
            //execution
            steps{

                println("Stage 2 - Test");
                println("Running some tests ...");
            
            }//end steps

        }//end stage

        
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Scan
        // 
        // Precondition: The application should be built already at this stage, the pipeline script should already be installed and should be in the pipeline-scan directory in the pwd()
        // Postcondition: Will run Veracodes Pipeline scanner in the pipeline and display the results passing or failing depending on the settings and CWE found
        stage("Stage 3 - Pipeline Scan "){
            
            
        
            //execution
            steps{
                catchError(buildResult: 'NOT_BUILT') {

                    println("------------------------------ Veracode Pipeline Scanner -----------------------------");
                    println("Current Working Directory: " + pwd());
                    
                    //groovy script
                    script{
                        //utilizing the withCredentials function block that takes the id of the variables set. Note there are additioanl ways of configuring the app id and key depending on if they have been marked as secret or not or as credentials
                        withCredentials([usernamePassword(credentialsId: 'veracode_login', passwordVariable: 'veracode_api_key', usernameVariable: 'veracode_api_id')]) {
                            
                            //Conditiona;
                            if(isUnix() == false){ 
                                //windows
                                echo '---------------------------- Pipeline scan starting - Windows based -----------------------------'
                                
                                powershell '''java -jar pipeline-scan/pipeline-scan.jar -f app/target/verademo.war --veracode_api_id "$env:veracode_api_id" -vkey "$env:veracode_api_key" -f app/target/verademo.war  '''
                                
                                
                            }//end if
                            else{
                                //linux
                                echo '---------------------------- Pipeline scan starting - Unix based -----------------------------'
                                
                                sh '''java -jar pipeline-scan/pipeline-scan.jar -f app/target/verademo.war --veracode_api_id "$env:veracode_api_id" -vkey "$env:veracode_api_key" -f app/target/verademo.war  || true ''' 
                                
                            }//end else
                        }//end withCredentials
                    
                    }//end script
                }
            }//end steps
    
        }//end stage    
        
        
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // SCA
        // 
        // Precondition: The previous stages should be completed, otherwise will skip the stage if the pipeline failed in another stage
        // Postcondition: Deploying the applicaiton
        stage("Stage 4 - SCA"){
            steps{
                script{
                    dir("app"){
                        withCredentials([string(credentialsId: 'SRCCLR_API_TOKEN', variable: 'SRCCLR_API_TOKEN')]) {
                            if(isUnix() == true){
                                //sh 'curl -sSL https://download.sourceclear.com/ci.sh | sh'
                                sh "curl -sSL https://download.sourceclear.com/ci.sh | DEBUG=1 sh -s -- scan --no-upload"
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

    }//end stages



}//end pipeline

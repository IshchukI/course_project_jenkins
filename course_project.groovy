pipeline {
    agent 'any'
    stages {
        stage('Create docker image') {
            steps {
                sh 'rm -rf folder_for_docker_file'
                dir ("folder_for_docker_file") {
                    sh 'echo $USER' // выыести пользователя
                    sh 'docker pull ubuntu:18.04' //скачать имэдж
                    sh 'git clone https://github.com/IshchukI/course_project.git'//скачать доккер файл
                    sh 'docker build --no-cache -f course_project/Dockerfile . -t course_proj' //из доккер файла создать имэдж
               }
            }
        }
        stage('Create docker container') {
            steps {
                sh 'docker stop $(docker ps -a -q)'  //остановить все кнотейнеры
                sh 'docker rm $(docker ps -a -q)' //удвлить все контейнеры
                sh 'docker run -v /tmp/reports/allure-results:/tmp/reports/allure-results --name course_project_container -id course_proj' // создать контейнер из имєджа
            }
        }
        stage('Setup utils'){
            steps {
                sh 'docker exec course_project_container sh -c \"apt-get update\"'
                sh 'docker exec course_project_container sh -c \"apt-get install -y python3.8\"'
                sh 'docker exec course_project_container sh -c \"apt-get install -y python3-pip\"'
                sh 'docker exec course_project_container sh -c \"apt-get install -y git\"'
                sh 'docker exec course_project_container sh -c \"apt-get install -y python3.8-venv\"'
                sh 'docker exec course_project_container sh -c \"pip3 install pytest\"'
                sh 'docker exec course_project_container sh -c \"update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.6 1\"'
                sh 'docker exec course_project_container sh -c \"update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.8 2\"'

            }
        }

        stage('Run project'){
            steps {
                sh 'docker exec course_project_container sh -c \"git clone https://github.com/IshchukI/PlayWrite.git\"'
                sh 'docker exec course_project_container sh -c \"python3 -m venv PlayWrite/playwrite_venv\"'
                sh 'docker exec course_project_container sh -c \". PlayWrite/playwrite_venv/bin/activate\"'
                sh 'docker exec course_project_container sh -c \"cd PlayWrite/ && pip3 install -e . && pip3 install -e src/ && python3 -m playwright install\"'
                sh 'docker exec course_project_container sh -c \"python3 -m pytest PlayWrite/dummy_test.py -p no:cacheprovider --alluredir=/tmp/reports/allure-results\"'
                sh 'docker exec course_project_container sh -c \"playwright install-deps\"'
                sh 'docker exec course_project_container sh -c \"apt-get install -y libnss3 libnspr4 libatk1.0-0 libatk-bridge2.0-0 libcups2 libdrm2 libxkbcommon0 libxcomposite1 libxdamage1 libxfixes3 libxrandr2 libgbm1 libpango-1.0-0 libcairo2 libasound2 libatspi2.0-0 libwayland-client0\"'
                sh 'docker exec course_project_container sh -c \"python3 -m pytest PlayWrite/test\"'
            }
        }
    }
}


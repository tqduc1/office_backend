DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

TAG="latest"

rm -rf build/

./gradlew bootJar -DskipTests

docker container stop $(docker ps -aqf "name=^office-backend-uat")

docker container rm $(docker ps -aqf "name=^office-backend-uat")

docker rmi $(docker images 'tdx-images/office-backend-uat' -q)

docker build -t tdx-images/office-backend-uat:$TAG -f $DIR/Dockerfile $DIR

docker tag tdx-images/office-backend-uat:latest tdx-images/office-backend-uat:$TAG

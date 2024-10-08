Container run

docker build -t megogo-tests .
docker run -p 4040:4040 megogo-tests

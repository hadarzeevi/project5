# compile and run a C program in a Docker container
FROM ubuntu:22.04 AS builder

# install build tools
RUN apt-get update && apt-get install -y build-essential

# set the working directory
WORKDIR /app

COPY . .

RUN make

FROM ubuntu:22.04

WORKDIR /app

COPY --from=builder /app/my_program .

CMD ["./my_program", "8080", "8", "x", "x"]

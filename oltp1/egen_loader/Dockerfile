# First stage: build the binary
FROM gcc:15-bookworm AS builder

# Set work directory
WORKDIR /build

# Copy the zip file into the image
COPY tpc-e-tool.zip .

# Unzip the source
RUN apt-get update && apt-get install -y unzip && \
    unzip tpc-e-tool.zip
	
WORKDIR /build/prj

# Run make to compile
RUN make CCFLAGS="-static-libstdc++ -static-libgcc" LDFLAGS="-static-libstdc++ -static-libgcc"

# Second stage: only include the binary
FROM debian:bookworm-slim

# Set working directory
WORKDIR /app

# Copy the binary from the build stage
COPY --from=builder /build/bin/EGenLoader .

# Run the binary by default (optional)
ENTRYPOINT ["./EGenLoader"]
CMD []
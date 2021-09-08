echo 'var providedBenchmarks = ["OpenJDK 17", "GraalVM CE Java 17", "GraalVM CE Java 17 patched"];
var providedBenchmarkStore = {
"OpenJDK 17":'
jq -c . < openjdk-17.json
echo ', "GraalVM CE Java 17":'
jq -c . < graalvm-ce-java17.json
echo ', "GraalVM CE Java 17 patched":'
jq -c . < graalvm-ce-java17-patched.json
echo '}'

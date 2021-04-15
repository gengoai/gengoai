BASE_DIR=$(dirname "$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)")
OUT_DIR="$BASE_DIR/target/hermes/"
BUILD_DIR="$BASE_DIR/release/distribution"

# Make sure everything is building and tests pass
mvn clean install || exit

#Prepare the output directory
mkdir -p "$OUT_DIR" &>/dev/null

#Copy over the scripts and resources
cp "${BUILD_DIR:?}/scripts/*" "$OUT_DIR" || exit
cp -r "${BUILD_DIR}/resources/"* "$OUT_DIR" || exit
cd "$BUILD_DIR" || exit

#Remove any existing lib or spark directories
rm -rf "${OUT_DIR:?}/lib/" &>/dev/null
rm -rf "${OUT_DIR:?}/spark/" &>/dev/null

#Copy over the runtime dependencies
mvn dependency:copy-dependencies -DoutputDirectory="${OUT_DIR:?}/lib/" -DincludeScope=runtime

#Remove test dependencies
rm "${OUT_DIR:?}/lib/junit*" &>/dev/null
rm "${OUT_DIR:?}/lib/hamcrest*" &>/dev/null
rm "${OUT_DIR:?}/lib/metainf-services*" &>/dev/null

#Build the core hermes distribution
cd "$OUT_DIR" || exit
tar -zcf ../hermes.tar.gz *

#Copy the spark requirements
cd "$BUILD_DIR" || exit
mvn dependency:copy-dependencies -DoutputDirectory="${OUT_DIR:?}/spark/" -DexcludeScope=runtime -DincludeScope=provided

#Build the spark lib distribution
cd "$OUT_DIR" || exit
tar -zcf ../hermes_spark.tar.gz spark/*

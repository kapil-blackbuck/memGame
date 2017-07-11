if [[ $KEYSTORE && ${KEYSTORE} && $KEYSTORE_URI && ${KEYSTORE_URI} ]]
then
    echo "Keystore detected - downloading..."
    curl -L -o ${KEYSTORE} ${KEYSTORE_URI}
else
    echo "Keystore uri not set.  .APK artifact will not be signed."
fi
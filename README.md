# Special Instructions
## Google Login Functionality: When running code locally (Google Login works in APK)

When running the code locally, the Google login functionality might not work due to the absence of an SHA-1 key linked with your Firebase project. This key is crucial for authenticating your app's digital signature with Google services.

### To Enable Google Login:

1. Local Code Execution:

- When running the code on your local machine for testing or development purposes, the Google login might not function due to the absence of the necessary credentials.
  
2. APK Deployment:

- To enable Google login functionality, generate an SHA-1 key and add it to your Firebase project.
- Once the APK is compiled and installed on a device, Google login will function as expected since the app is then associated with the proper credentials linked to your Firebase project.

### Adding SHA-1 Key to Firebase:

1. Access your Firebase project settings.
2. Navigate to the section for adding app fingerprints or credentials.
3. Generate an SHA-1 key for your app and associate it with your Firebase project.
4. Once the SHA-1 key is added, rebuild your APK with this updated configuration.

Note: Ensure that the SHA-1 key used for the Firebase project matches the digital signature of the app in the deployed APK. This will authenticate the app with Google services and enable the Google login functionality when running the APK on devices.

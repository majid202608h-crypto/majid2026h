import re

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

pattern = r'<activity\s*android:name="\.MainActivity"'
replacement = '<activity\n            android:screenOrientation="portrait"\n            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden|smallestScreenSize"\n            android:name=".MainActivity"'

new_content = re.sub(pattern, replacement, content)

with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(new_content)

print("Replaced!")

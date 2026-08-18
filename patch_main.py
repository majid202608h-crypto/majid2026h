with open('./app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

replacement = """
    LaunchedEffect(Unit) {
        com.example.data.SubscriptionManager.initFirstLaunchIfNeeded(context)
        firstLaunchTime = com.example.data.SubscriptionManager.getFirstLaunchTime(context)
        
        // Periodically check license status to react to cloud sync or expiration
        while (true) {
            kotlinx.coroutines.delay(2000)
            firstLaunchTime = com.example.data.SubscriptionManager.getFirstLaunchTime(context)
            permanentLicensed = com.example.data.SubscriptionManager.isPermanentLicensed(context)
            licenseExpiryTime = (com.example.data.SubscriptionManager.getSubscriptionDetails(context)["expireTime"] as? Long) ?: 0L
        }
    }
"""

content = content.replace("""
    LaunchedEffect(Unit) {
        com.example.data.SubscriptionManager.initFirstLaunchIfNeeded(context)
        firstLaunchTime = com.example.data.SubscriptionManager.getFirstLaunchTime(context)
    }
""".strip(), replacement.strip())

with open('./app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

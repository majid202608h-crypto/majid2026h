import re

with open('./app/src/main/java/com/example/ui/GameViewModel.kt', 'r') as f:
    content = f.read()

# buildCloudBackupJson
backup_replacement = """
            rewardRepository?.let { repo ->
                root.put("wheel", repo.exportForCloud())
            }
            
            // Append License info to Cloud backup
            val licObj = org.json.JSONObject()
            licObj.put("firstLaunchTime", com.example.data.SubscriptionManager.getFirstLaunchTime(context))
            licObj.put("permanentLicensed", com.example.data.SubscriptionManager.isPermanentLicensed(context))
            licObj.put("expireTime", (com.example.data.SubscriptionManager.getSubscriptionDetails(context)["expireTime"] as? Long) ?: 0L)
            root.put("license", licObj)
            
            root.toString()
"""
content = content.replace("""            rewardRepository?.let { repo ->
                root.put("wheel", repo.exportForCloud())
            }
            root.toString()""", backup_replacement.strip())


# applyCloudRestore
restore_replacement = """
            root.optJSONObject("wheel")?.let { wheelObj ->
                val cloudLastSpinWall = wheelObj.optLong("lastSpinWall", 0L)
                val cloudSpinCount = wheelObj.optInt("spinCount", 0)
                rewardRepository?.reconcileFromCloud(cloudLastSpinWall, cloudSpinCount)
            }
            
            root.optJSONObject("license")?.let { licObj ->
                val cloudFirstLaunch = licObj.optLong("firstLaunchTime", 0L)
                val cloudPermanent = licObj.optBoolean("permanentLicensed", false)
                val cloudExpiry = licObj.optLong("expireTime", 0L)
                
                // Reconcile
                if (cloudPermanent) {
                    com.example.data.SubscriptionManager.setPermanentLicensed(context, true)
                }
                
                val currentFirstLaunch = com.example.data.SubscriptionManager.getFirstLaunchTime(context)
                // If cloud has an older (smaller) first launch time, we should use it so user doesn't get infinite trials
                if (cloudFirstLaunch > 0L && (currentFirstLaunch == 0L || cloudFirstLaunch < currentFirstLaunch)) {
                    val prefs = context.getSharedPreferences("game_license_pref", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putLong("first_launch_time", cloudFirstLaunch).apply()
                }
                
                val currentExp = (com.example.data.SubscriptionManager.getSubscriptionDetails(context)["expireTime"] as? Long) ?: 0L
                if (cloudExpiry > currentExp) {
                    val prefs = context.getSharedPreferences("game_license_pref", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putLong("expireTime", cloudExpiry).apply()
                }
            }
"""
content = content.replace("""            root.optJSONObject("wheel")?.let { wheelObj ->
                val cloudLastSpinWall = wheelObj.optLong("lastSpinWall", 0L)
                val cloudSpinCount = wheelObj.optInt("spinCount", 0)
                rewardRepository?.reconcileFromCloud(cloudLastSpinWall, cloudSpinCount)
            }""", restore_replacement.strip())

with open('./app/src/main/java/com/example/ui/GameViewModel.kt', 'w') as f:
    f.write(content)


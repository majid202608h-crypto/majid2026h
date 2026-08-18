import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace texts and sizes
content = content.replace('text = "سفر گام‌به‌گام",\n                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),\n                                fontWeight = FontWeight.Medium,\n                                fontSize = 11.sp', 'text = "سفر گام به گام",\n                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),\n                                fontWeight = FontWeight.Medium,\n                                fontSize = 13.sp')

content = content.replace('text = "مرور و تمرین خانه",\n                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),\n                                fontWeight = FontWeight.Medium,\n                                fontSize = 11.sp', 'text = "مرور و تمرین",\n                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),\n                                fontWeight = FontWeight.Medium,\n                                fontSize = 13.sp')

content = content.replace('text = "رکورد: $bestSpeed امتیاز",\n                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),\n                                fontWeight = FontWeight.Medium,\n                                fontSize = 11.sp', 'text = "رکورد: $bestSpeed امتیاز",\n                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),\n                                fontWeight = FontWeight.Medium,\n                                fontSize = 13.sp')

content = content.replace('text = "جواب رو پیدا کن",\n                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),\n                                fontWeight = FontWeight.Medium,\n                                fontSize = 11.sp', 'text = "جواب را پیدا کن",\n                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),\n                                fontWeight = FontWeight.Medium,\n                                fontSize = 13.sp')

content = content.replace('text = "نبرد دونفره",\n                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),\n                                fontWeight = FontWeight.Medium,\n                                fontSize = 11.sp', 'text = "نبرد دو نفره",\n                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),\n                                fontWeight = FontWeight.Medium,\n                                fontSize = 13.sp')

content = content.replace('text = "خرید آواتار",\n                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),\n                                fontWeight = FontWeight.Medium,\n                                fontSize = 11.sp', 'text = "خرید آواتار",\n                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),\n                                fontWeight = FontWeight.Medium,\n                                fontSize = 13.sp')

# To fix spacing, in the grid column:
# verticalArrangement = Arrangement.spacedBy(8.dp) -> 12.dp
content = content.replace('verticalArrangement = Arrangement.spacedBy(8.dp)\n            ) {\n                // Row 1', 'verticalArrangement = Arrangement.spacedBy(12.dp)\n            ) {\n                // Row 1')

# horizontalArrangement = Arrangement.spacedBy(10.dp) -> 12.dp
content = content.replace('horizontalArrangement = Arrangement.spacedBy(10.dp)', 'horizontalArrangement = Arrangement.spacedBy(12.dp)')

# Remove Spacer(modifier = Modifier.height(4.dp)) between Row 1, Row 2, Row 3
content = content.replace('                }\n\n                Spacer(modifier = Modifier.height(4.dp))\n\n                // Row 2', '                }\n\n                // Row 2')
content = content.replace('                }\n\n                                Spacer(modifier = Modifier.height(4.dp))\n\n                // Row 3', '                }\n\n                // Row 3')


with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

import os
import glob

paths = glob.glob('D:/Documents/GitHub/Cat-Tastic-POS/app/src/main/java/**/*.kt', recursive=True)
for p in paths:
    with open(p, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Replace %.2f with %.0f to remove decimals
    new_content = content.replace('"%.2f"', '"%.0f"')
    
    if content != new_content:
        with open(p, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated {p}")

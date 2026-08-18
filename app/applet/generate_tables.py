import os
import asyncio
import edge_tts

def num_to_persian_word(n):
    ones = {
        0: "", 1: "یک", 2: "دو", 3: "سه", 4: "چهار", 5: "پنج", 
        6: "شش", 7: "هفت", 8: "هشت", 9: "نه", 10: "ده",
        11: "یازده", 12: "دوازده", 13: "سیزده", 14: "چهارده", 15: "پانزده",
        16: "شانزده", 17: "هفده", 18: "هجده", 19: "نوزده"
    }
    tens = {
        20: "بیست", 30: "سی", 40: "چهل", 50: "پنجاه", 
        60: "شصت", 70: "هفتاد", 80: "هشتاد", 90: "نود"
    }
    if n in ones:
        return ones[n]
    if n in tens:
        return tens[n]
    
    t = (n // 10) * 10
    o = n % 10
    return f"{tens[t]} و {ones[o]}"

async def generate_file(a, b, output_dir):
    file_name = f"mul_{a}_{b}.mp3"
    file_path = os.path.join(output_dir, file_name)
    
    text = f"{num_to_persian_word(a)} ضربدر {num_to_persian_word(b)} مساوی {num_to_persian_word(a * b)}"
    print(f"Generating: {text} -> {file_path}")
    
    # We will use Dilara voice which is warm and friendly for children
    communicate = edge_tts.Communicate(text, "fa-IR-DilaraNeural")
    
    # Retry logic up to 3 times
    for attempt in range(3):
        try:
            await communicate.save(file_path)
            # Verify file is not empty and is valid size
            if os.path.exists(file_path) and os.path.getsize(file_path) > 1000:
                print(f"Successfully saved: {file_name}")
                return True
        except Exception as e:
            print(f"Attempt {attempt+1} failed for {file_name}: {e}")
            await asyncio.sleep(1)
    print(f"ERROR: Failed to generate {file_name}")
    return False

async def main():
    output_dir = "/app/src/main/res/raw"
    os.makedirs(output_dir, exist_ok=True)
    
    # Generate 1x1 to 9x9 (81 files)
    tasks = []
    sem = asyncio.Semaphore(5)  # Limit concurrency to 5 requests to avoid rate limits
    
    async def sem_worker(a, b):
        async with sem:
            return await generate_file(a, b, output_dir)
            
    for a in range(1, 10):
        for b in range(1, 10):
            tasks.append(sem_worker(a, b))
            
    results = await asyncio.gather(*tasks)
    success_count = sum(1 for r in results if r)
    print(f"\nCompleted! Generated {success_count} / 81 files successfully.")

if __name__ == "__main__":
    asyncio.run(main())

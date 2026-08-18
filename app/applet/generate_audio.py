import os
from gtts import gTTS

def generate_multiplication_tables():
    output_dir = "app/src/main/res/raw"
    os.makedirs(output_dir, exist_ok=True)
    
    print("Generating missing multiplication audio files (up to 12x12)...")
    generated_count = 0
    for a in range(1, 13):
        for b in range(1, 13):
            file_name = f"mul_{a}_{b}.mp3"
            file_path = os.path.join(output_dir, file_name)
            
            # Skip if file already exists (so we don't overwrite pre-recorded 1x1 to 9x9 files)
            if os.path.exists(file_path):
                continue
                
            text = f"{a} ضربدر {b} مساوی {a * b}"
            print(f"Generating: {text} -> {file_path}")
            try:
                tts = gTTS(text=text, lang='fa')
                tts.save(file_path)
                generated_count += 1
            except Exception as e:
                print(f"Error generating {file_name}: {e}")
                
    print(f"Successfully generated {generated_count} multiplication audio files.")

def generate_game_sounds():
    output_dir = "app/src/main/res/raw"
    os.makedirs(output_dir, exist_ok=True)
    
    sounds = {
        "correct_01.mp3": "آفرین! صد آفرین!",
        "correct_02.mp3": "عالی پاسخ دادی!",
        "correct_03.mp3": "فوق‌العاده و محشر!",
        "wrong_01.mp3": "اشکالی نداره! دفعه بعد حتماً درست می‌زنی.",
        "wrong_02.mp3": "با تمرین بیشتر قوی‌تر می‌شی!",
        "finish_01.mp3": "مرحله با موفقیت به پایان رسید! آفرین بر تو قهرمان!",
        "boss_start_01.mp3": "مبارزه با نگهبان اژدهای بزرگ ضرب شروع شد! آماده‌ای؟",
        "boss_win_01.mp3": "تبریک می‌گم! تو اژدهای بزرگ رو شکست دادی و قهرمان شدی!",
        "boss_lose_01.mp3": "اشکالی نداره، اژدها این بار قوی‌تر بود. دوباره تلاش کن!",
        "wheel_start.mp3": "گردونه شانس شروع به چرخش کرد!",
        "wheel_spin.mp3": "گردونه شانس در حال چرخشه...",
        "wheel_win.mp3": "تبریک می‌گم! جایزه گردونه شانس رو بردی!",
        "wheel_finish.mp3": "جایزه با موفقیت دریافت شد!"
    }
    
    print("Generating custom Persian spoken game sounds...")
    generated_count = 0
    for file_name, text in sounds.items():
        file_path = os.path.join(output_dir, file_name)
        if os.path.exists(file_path):
            continue
            
        print(f"Generating sound: '{text}' -> {file_path}")
        try:
            tts = gTTS(text=text, lang='fa')
            tts.save(file_path)
            generated_count += 1
        except Exception as e:
            print(f"Error generating game sound {file_name}: {e}")
            
    print(f"Successfully generated {generated_count} game sound files.")

if __name__ == "__main__":
    generate_multiplication_tables()
    generate_game_sounds()
    print("All audio generation completed successfully!")

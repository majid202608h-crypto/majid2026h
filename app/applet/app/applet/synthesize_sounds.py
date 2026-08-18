import os
import math
import wave
import struct

def write_wav(filename, sample_rate, duration, generator_func):
    num_samples = int(sample_rate * duration)
    with wave.open(filename, 'wb') as wav_file:
        wav_file.setnchannels(1)
        wav_file.setsampwidth(2)
        wav_file.setframerate(sample_rate)
        
        for i in range(num_samples):
            t = float(i) / sample_rate
            value = generator_func(t, duration)
            # Clip value to 16-bit range (-32768 to 32767)
            val = int(max(-1.0, min(1.0, value)) * 32767)
            wav_file.writeframesraw(struct.pack('<h', val))

def generate_all_sounds():
    output_dir = "app/src/main/res/raw"
    os.makedirs(output_dir, exist_ok=True)
    sample_rate = 44100
    
    # 1. Correct sound 01 (Rising sharp ding)
    def correct_01(t, dur):
        freq = 600 + (1200 - 600) * (t / dur)
        phase = 2 * math.pi * freq * t
        amplitude = math.exp(-6 * t)
        return amplitude * math.sin(phase)
        
    # 2. Correct sound 02 (Pleasant high double chime)
    def correct_02(t, dur):
        if t < 0.15:
            freq = 1046.50  # C6
            amp = math.exp(-15 * t)
        elif t < 0.2:
            return 0.0
        else:
            freq = 1318.51  # E6
            amp = math.exp(-15 * (t - 0.2))
        phase = 2 * math.pi * freq * t
        return amp * math.sin(phase)
        
    # 3. Correct sound 03 (Sparkling vibra-chime)
    def correct_03(t, dur):
        vibrato = 50 * math.sin(2 * math.pi * 30 * t)
        freq = 1500 + vibrato
        phase = 2 * math.pi * freq * t
        amp = math.exp(-8 * t)
        return amp * math.sin(phase)
        
    # 4. Wrong sound 01 (Low buzz)
    def wrong_01(t, dur):
        freq = 180 - 40 * (t / dur)
        phase1 = 2 * math.pi * freq * t
        phase2 = 2 * math.pi * (freq * 2) * t
        amp = math.exp(-4 * t)
        return amp * (0.7 * math.sin(phase1) + 0.3 * math.sin(phase2))
        
    # 5. Wrong sound 02 (Sad descending buzz)
    def wrong_02(t, dur):
        freq = 300 - 200 * (t / dur)
        phase = 2 * math.pi * freq * t
        amp = math.exp(-5 * t)
        return amp * math.sin(phase)
        
    # 6. Finish sound 01 (cheerful major triad arpeggio)
    def finish_01(t, dur):
        note_dur = dur / 4.0
        note_idx = int(t / note_dur)
        notes = [523.25, 659.25, 783.99, 1046.50]  # C5, E5, G5, C6
        freq = notes[min(note_idx, 3)]
        local_t = t % note_dur
        amp = math.exp(-4 * local_t)
        phase = 2 * math.pi * freq * t
        return amp * math.sin(phase)
        
    # 7. Boss start sound 01 (Heavy dramatic pitch swell horn)
    def boss_start_01(t, dur):
        freq = 110 + 20 * math.sin(2 * math.pi * 5 * t)
        phase1 = 2 * math.pi * freq * t
        phase2 = 2 * math.pi * (freq * 1.5) * t
        amp = (1.0 - math.exp(-10 * t)) * math.exp(-2 * t)
        return amp * (0.6 * math.sin(phase1) + 0.4 * math.sin(phase2))
        
    # 8. Boss win sound 01 (Extended triumphant fanfare)
    def boss_win_01(t, dur):
        note_dur = dur / 6.0
        note_idx = int(t / note_dur)
        notes = [523.25, 783.99, 523.25, 659.25, 783.99, 1046.50]
        freq = notes[min(note_idx, 5)]
        local_t = t % note_dur
        amp = math.exp(-3 * local_t)
        phase = 2 * math.pi * freq * t
        return amp * math.sin(phase)
        
    # 9. Boss lose sound 01 (Tragic descending game-over theme)
    def boss_lose_01(t, dur):
        note_dur = dur / 4.0
        note_idx = int(t / note_dur)
        notes = [440.00, 392.00, 349.23, 329.63]
        freq = notes[min(note_idx, 3)]
        local_t = t % note_dur
        amp = math.exp(-3 * local_t)
        phase = 2 * math.pi * freq * t
        return amp * math.sin(phase)
        
    # 10. Wheel start (Cute pop)
    def wheel_start(t, dur):
        freq = 300 + 1200 * (t / dur)
        phase = 2 * math.pi * freq * t
        amp = math.exp(-30 * t)
        return amp * math.sin(phase)
        
    # 11. Wheel spin (Short mechanical tick)
    def wheel_spin(t, dur):
        freq = 800 - 400 * (t / dur)
        phase = 2 * math.pi * freq * t
        amp = math.exp(-100 * t)
        return amp * math.sin(phase)
        
    # 12. Wheel win (Celebratory chime cascade)
    def wheel_win(t, dur):
        freq = 800 + 800 * math.sin(2 * math.pi * 5 * t)
        phase = 2 * math.pi * freq * t
        amp = math.exp(-3 * t)
        return amp * math.sin(phase)
        
    # 13. Wheel finish (Cha-ching register)
    def wheel_finish(t, dur):
        if t < 0.1:
            freq = 2500
            amp = math.exp(-40 * t)
        elif t < 0.15:
            return 0.0
        else:
            freq = 2800
            amp = math.exp(-40 * (t - 0.15))
        phase = 2 * math.pi * freq * t
        return amp * math.sin(phase)

    sound_configs = [
        ("correct_01.wav", 0.5, correct_01),
        ("correct_02.wav", 0.5, correct_02),
        ("correct_03.wav", 0.6, correct_03),
        ("wrong_01.wav", 0.5, wrong_01),
        ("wrong_02.wav", 0.6, wrong_02),
        ("finish_01.wav", 1.2, finish_01),
        ("boss_start_01.wav", 1.5, boss_start_01),
        ("boss_win_01.wav", 1.8, boss_win_01),
        ("boss_lose_01.wav", 1.5, boss_lose_01),
        ("wheel_start.wav", 0.2, wheel_start),
        ("wheel_spin.wav", 0.1, wheel_spin),
        ("wheel_win.wav", 1.2, wheel_win),
        ("wheel_finish.wav", 0.4, wheel_finish),
    ]

    print("Synthesizing clean PCM WAV files locally...")
    for filename, duration, generator_func in sound_configs:
        file_path = os.path.join(output_dir, filename)
        # Ensure we write to correct path
        print(f"Synthesizing: {filename} ({duration}s) -> {file_path}")
        write_wav(file_path, sample_rate, duration, generator_func)
        
    print("All custom synthesized WAV files generated successfully!")

if __name__ == "__main__":
    generate_all_sounds()

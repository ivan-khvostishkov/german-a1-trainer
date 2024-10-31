# German Hands-Free Trainer

German Hands-free Trainer is the tool that narrates phrases from German A1 / A2 / B1 [Goethe-Zertifikat](https://www.goethe.de/de/spr/kup/prf/prf/sd1/inf.html) exams.

It works by downloading the "Wortliste" PDF with German words and sample phrases from the Goethe Insitut website, parsing it with [Amazon Textract](https://aws.amazon.com/textract/), extracting and translating the phrases from German into English by [Amazon Translate](https://aws.amazon.com/translate/) and narrating the phrases with [Amazon Polly](https://aws.amazon.com/polly/). Finally, it assembles the audio files into a video file and hardcodes subtitles with [FFMpeg](https://www.ffmpeg.org/) using the speech marks that Polly produces during speech-to-text conversion.

Check the result and the uploaded trainer videos on my YouTube channel:

* German A1 (Start Deutsch 1): https://youtu.be/tSCrnPUt-pU
* German A2: TBD
* German B1: TBD

To prepare for the exam, I recommend that you play the video in the Firefox browser on your mobile device and listen to it carefully through headphones from the locked screen while walking outside, breathing the fresh air and keeping your hands free from your phone.

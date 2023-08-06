# German A1 Trainer

German A1 Trainer is the tool that narrates phrases from German A1 Word List for the "[Goethe-Zertifikat A1: Start Deutsch 1](https://www.goethe.de/de/spr/kup/prf/prf/sd1/inf.html)" exam.

It works by downloading the "Wortliste" PDF with approximately 650 German words and 850 sample phrases from the Goete Insitut website, parsing it with [Amazon Textract](https://aws.amazon.com/textract/), extracting and translating the phrases from German into English by [Amazon Translate](https://aws.amazon.com/translate/) and narrating the phrases with [Amazon Polly](https://aws.amazon.com/polly/). Finally, it assembles the audio files into a video file and hardcodes subtitles with [FFMpeg](https://www.ffmpeg.org/).

Check the result and the uploaded trainer video on YouTube:

https://youtube.com/    

When preparing for the exam, play the video in the Firefox browser on your mobile device and listen to it carefully through headphones from the locked screen while walking outside and breathing the fresh air.

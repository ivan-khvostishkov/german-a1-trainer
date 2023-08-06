# German A1 Trainer Video

German A1 Trainer Video narrates the phrases and their translations into English from German A1 Word List for the "[Goethe-Zertifikat A1: Start Deutsch 1](https://www.goethe.de/de/spr/kup/prf/prf/sd1/inf.html)" exam.

When preparing for the exam, play the video in the Firefox browser on your mobile device and listen to it carefully through headphones from the locked screen while walking outside and breathing the fresh air.

The video is produced with the German A1 Trainer tool that  works by downloading the "Wortliste" PDF with approximately 650 German words and 850 sample phrases from the Goete Insitut website, parsing it with [Amazon Textract](https://aws.amazon.com/textract/), extracting and translating the phrases from German into English by [Amazon Translate](https://aws.amazon.com/translate/) and narrating the phrases with [Amazon Polly](https://aws.amazon.com/polly/).

With [FFMpeg](https://www.ffmpeg.org/), it assembles the audio files with the phrases in MP3 format into an MP4 video file with hardcoded subtitles. 

Check the source code of the tool on GitHub:

https://github.com/ivan-khvostishkov/german-a1

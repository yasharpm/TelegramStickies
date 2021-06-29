# TelegramStickies
Sticker manipulator for Telegram (it also works for manipulating Lottie animations)

https://user-images.githubusercontent.com/4597931/123779452-6ea0a980-d8e7-11eb-89bc-127260d2c4a3.mov

- The SDK module is named stickysdk
- You can see it being used in the app module
- The work is not complete but no major changes will happen. Mostly only bug fixes.
- You can translate, scale, rotate and change colors of a sticker
- You can combine multiple stickers, reorder and remove them.
- Then you get the result as a single JSON which you can save into a file with .tgs (or .json) extension.
- I am not validating Telegram sticker standards, so take caution!
- All animations are assumed to have 60fps. You can get the duration in seconds by dividing the duration by 60.
- I will *not* post notifications when I update it. Sorry! I'm expecting to tweak it very frequently. 

For long-running or CPU-intensive tasks, there are basically two ways to do this: Java threads, and Android's native AsyncTask.

Neither one is necessarily better than the other, but knowing when to use each call is essential to leveraging the system's performance to your benefit.

Use AsyncTask for:

Simple network operations which do not require downloading a lot of data
Disk-bound tasks that might take more than a few milliseconds

Use Java threads for:

Network operations which involve moderate to large amounts of data (either uploading or downloading)
High-CPU tasks which need to be run in the background
Any task where you want to control the CPU usage relative to the GUI thread
And there are lot of good resources over internet which may help you:

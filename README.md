myStatus
========

myStatus is an Android app that provides self-management tools to users
with chronic health conditions. It helps users manage their medications,
stay in touch with their clinicians, and report their status by responding
to surveys. The survey rendering and submission components are based on
[ODK Collect](http://opendatakit.org/use/collect/).

Features
--------

  - Collects user's status information using surveys specified in the
    [XForms](http://en.wikipedia.org/wiki/Xforms) format. Clinicians can
    also use ODK's [XLSForm](http://opendatakit.org/use/xlsform/) tool to
    create surveys.

  - Submits collected data to the clinician via a server running an
    [ODK Aggregate](http://opendatakit.org/use/aggregate/) instance.

  - Displays history of survey responses to show progress over time.

  - Tracks user's prescriptions and reminds the user to take them.

  - Provides quick-dial buttons for important contacts.


Getting myStatus
===============

Binary Distribution
-------------------

A binary version of myStatus is hosted irregularly, based on recent
development.

You can install the most recent version of myStatus by navigating to this
address in your Android phone's browser: <http://db.tt/y1vgEDpg>

You can also install myStatus by scanning this QR code:

![myStatus QR Code](mystatus-download-qr-code.png)

You will need to enable installation of apps from [unknown sources] [1]
before installing myStatus.

[1]: http://developer.android.com/distribute/open.html#unknown-sources

From Source
-----------

### Eclipse ###

Building myStatus from source requires the
[Android SDK](http://developer.android.com/sdk/index.html), and is easiest
using Eclipse and the ADT Eclipse plugin.

 1. Clone the repository by running the command:

        git clone https://github.com/rjbailey/mystatus.git

 2. Open Eclipse and select File > Import... > Existing Android Code Into
    Workspace.

 3. For the Root Directory, browse to the mystatus repo.

 4. Select both cachewordlib and myStatus and click Finish.

 5. Right click the myStatus project and open Properties.

 6. In the Android properties pane, select a Google APIs (Android + Google
    APIs) build target and click OK.

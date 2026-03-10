We are designing the architecture for an Android Jetpack Compose app called VTS3.

The goal is to recreate and improve the schedule workflow from an earlier app (VTS2).

Important architectural decisions already made:

APP STRUCTURE
-------------
Root architecture:

AppRoot
├── Setup gate (choose PassengerSchedules folder)
└── VtsMainShell

VtsMainShell
├── Bottom Navigation
└── NavHost

Bottom navigation destinations:

Schedule
Lookup
Drivers
Contacts
Clinics

Schedule is the start destination.

BOTTOM NAVIGATION MODEL
-----------------------
Destinations are defined using a sealed class:

sealed class AppDestination(val route: String, val label: String)

Objects:
Schedule
Lookup
Drivers
Contacts
Clinics

A list called bottomDestinations defines which screens appear in the bottom bar.

SCHEDULE DATA MODEL
-------------------

TripStatus:
ACTIVE
COMPLETED
REMOVED
CANCELLED
NOSHOW

TripViewMode (screen filter):
ACTIVE
COMPLETED
OTHER

OTHER is a bucket combining:
REMOVED
CANCELLED
NOSHOW

Trip data class:

Trip(
id: TripId,
date: LocalDate,
time: String,
name: String,
phone: String,
fromAddress: String,
toAddress: String,
status: TripStatus
)

Trip identity uses:

TripId = date | time | name | address

TripId.stable(date, name, time, address)

SCHEDULE LOADING
----------------

Schedules are loaded from .xls files in a user-selected folder.

XlsScheduleLoader:
- scans folder
- extracts dates from filenames
- loads base trips for a selected date

ScheduleRepository maintains:

ScheduleState(
selectedDate
allTripsForDate
availableDates
)

Counts are derived dynamically:

activeCount
completedCount
otherCount

STATUS BEHAVIOR
---------------

Active screen actions:
Complete
No Show
Cancel
Remove

Completed screen:
Reinstate

Other screen:
Reinstate
+ label showing reason (Cancel / No Show / Removed)

Trips in Other can be reinstated to Active.

PERSISTENCE MODEL
-----------------

Base schedules are read-only (.xls).

App state is stored using per-date JSON overlays:

completed-trips/
removed-trips/
inserted-trips/

Inserted trips are merged into the daily schedule.

ARCHITECTURE DECISION
---------------------

VTS3 will use:

UI → ViewModel → Repository

Repository responsibilities:
- load base schedule
- load overlays
- merge trips
- provide state

ViewModel responsibilities:
- selected TripViewMode
- UI actions
- calling repository methods
- transforming state for the screen

The Schedule screen will be rebuilt using this architecture.

Continue helping design the VTS3 architecture and implementation.


VTS3 Schedule Engine Design
Purpose

The VTS3 schedule engine is responsible for producing the final trip list for a selected schedule date.

It combines:

the base daily schedule from .xls

inserted trips saved by the app

persisted status changes saved by the app

The result is one unified List<Trip> that drives:

the Schedule screen

Active / Completed / Other filtering

live counts

reinstate behavior

Core Principle

The .xls schedule file is the read-only source schedule.

App-side changes are stored separately as overlays.

The visible schedule is rebuilt each time from:

Base Schedule + Inserted Trips + Status Overlays

This prevents corruption of the original schedule file and keeps app logic deterministic.

Inputs
1. Base Schedule File

One .xls file for the selected date.

Loaded from the user-selected PassengerSchedules folder.

Contains the original trips for the day.

2. Inserted Trips Overlay

Per-date JSON file containing trips manually added in the app.

Example:

inserted-trips/2026-03-09.json

These trips are merged into the day’s schedule and behave like normal trips.

3. Status Overlays

Per-date persisted status information for trips that are no longer plain ACTIVE.

Examples:

completed-trips/3-9-26.json
removed-trips/3-9-26.json

Future versions may unify these into a single status overlay file.

Statuses that matter to the engine are:

COMPLETED

REMOVED

CANCELLED

NOSHOW

Trips with no saved override remain ACTIVE.

Output

The schedule engine produces a final list of:

List<Trip>

Each trip in the final list must include:

TripId

date

time

name

phone

fromAddress

toAddress

status

This final list becomes the source of truth for the Schedule screen.

Trip Identity

Trips are matched using deterministic IDs.

Current formula
date | time | name | address

Example:

2026-03-09|13:40|larry colbert|123 main st

This allows a trip loaded from the .xls file to be matched with its saved overlays later.

Recommended stronger formula for VTS3

Consider using:

date | time | name | fromAddress | toAddress

This reduces collision risk when two trips share the same name, time, or pickup address.

Schedule Reconstruction Algorithm
Step 1 — Load Base Trips

Load the selected date’s .xls schedule file.

Parse rows into Trip objects.

Each base trip initially starts as:

status = TripStatus.ACTIVE

At this point, the engine has the raw schedule for the day.

Step 2 — Load Inserted Trips

Load the inserted-trip JSON file for the same date.

Convert inserted trip records into Trip objects.

Each inserted trip must also have a deterministic TripId.

Inserted trips should be added to the same working list as base trips.

At the end of this step:

workingTrips = baseTrips + insertedTrips
Step 3 — Load Status Overlays

Load all persisted status records for the selected date.

Examples:

completed trips

removed trips

cancelled trips

no-show trips

Convert those records into a map keyed by TripId.

Example conceptual structure:

Map<TripId, TripStatus>

Example:

Trip A -> COMPLETED
Trip B -> NOSHOW
Trip C -> REMOVED
Step 4 — Apply Status Overlays

Walk through workingTrips.

For each trip:

if a saved status override exists, apply it

otherwise keep status as ACTIVE

Conceptually:

finalTrips = workingTrips.map { trip ->
val overriddenStatus = statusOverrides[trip.id]
if (overriddenStatus != null) trip.copy(status = overriddenStatus)
else trip
}
Step 5 — Sort Final Trips

Sort the final list by trip time.

This keeps Active / Completed / Other views consistent and predictable.

Preferred sort:

parse time

sort by minutes since midnight

fallback safely for malformed times

Step 6 — Publish Final State

Expose the final list through repository state.

Example state:

ScheduleState(
selectedDate = ...,
allTripsForDate = finalTrips,
availableDates = ...,
isLoading = false,
errorMessage = null
)

This state becomes the source for:

counts

filtering

rendering

Filtering Model

The engine always produces the full final list for the selected date.

The UI does not load separate lists for Active / Completed / Other.

Instead, the view layer filters the same unified list.

Active view

Show trips where:

status == ACTIVE
Completed view

Show trips where:

status == COMPLETED
Other view

Show trips where:

status == REMOVED || status == CANCELLED || status == NOSHOW

This keeps counts and filtering consistent.

Count Model

Counts are always derived from the same final trip list.

Active count
allTripsForDate.count { it.status == TripStatus.ACTIVE }
Completed count
allTripsForDate.count { it.status == TripStatus.COMPLETED }
Other count
allTripsForDate.count { it.status.isOtherBucket() }

Because all counts are derived from the same list, they update instantly when status changes.

Status Change Algorithm

When the user taps an action button on a trip card, the engine must do two things:

update the in-memory trip list immediately

persist the change to the appropriate overlay store

Example: Complete

When user taps Complete:

update trip status in memory to COMPLETED

persist completed overlay for that trip

remove conflicting overlays if necessary

Result:

Active count decrements

Completed count increments

trip disappears from Active immediately

Example: No Show

When user taps No Show:

update trip status in memory to NOSHOW

persist no-show overlay

remove conflicting overlays

Result:

Active count decrements

Other count increments

trip appears under Other with label NO SHOW

Example: Cancel

When user taps Cancel:

update trip status in memory to CANCELLED

persist cancelled overlay

remove conflicting overlays

Example: Remove

When user taps Remove:

update trip status in memory to REMOVED

persist removed overlay

remove conflicting overlays

Example: Reinstate

When user taps Reinstate from Completed or Other:

update trip status in memory to ACTIVE

remove any saved non-active overlay for that trip

Result:

trip returns to Active

counts update immediately

Conflict Rule

A trip must have only one effective status at a time.

That means VTS3 should enforce this rule:

One trip → one non-active override max

If a trip becomes:

COMPLETED

REMOVED

CANCELLED

NOSHOW

then all other competing non-active overlay records for that same trip should be removed.

This prevents status collisions.

Repository Responsibilities

The schedule repository should own the engine workflow.

Repository should:

load available dates

load base trips

load inserted trips

load status overlays

merge the final daily schedule

expose ScheduleState

persist trip status changes

support reinstate behavior

Repository should not:

manage visual tab selection

manage dialog visibility

manage screen-only UI details

Those belong in the ViewModel.

ViewModel Responsibilities

The ViewModel sits between UI and repository.

ViewModel should:

hold selected TripViewMode

receive UI events

call repository methods

expose filtered lists for the current mode

expose counts and state to the UI

handle temporary UI actions like snackbars later

Failure Handling

The engine should handle these cases gracefully:

Missing schedule file

Return empty list for that date or show an error message.

Corrupt JSON overlay

Fail safely and treat that overlay as empty if possible.

Missing inserted trips file

Treat as no inserted trips.

Missing status files

Treat all trips as ACTIVE.

The Schedule screen should remain usable even when some overlay files are absent.

Future Evolution

The current design supports the existing separate overlay stores.

Later, VTS3 may simplify persistence by moving to:

status-overrides/<date>.json

with one unified structure:

Map<TripId, TripStatus>

That would make merge logic cleaner and reduce duplication.

This can be adopted later without changing the Schedule screen contract.

Summary

The VTS3 schedule engine works like this:

Load base trips from .xls

Load inserted trips from JSON

Merge them

Load saved status overlays

Apply statuses by TripId

Sort the final list

Expose one unified List<Trip>

All Schedule screen behavior should derive from that one final list.

This guarantees:

instant count updates

consistent filtering

clean reinstate behavior

persistent status changes

safe separation from the source schedule file
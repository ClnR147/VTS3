VTS3 Schedule Screen UI Contract
Purpose

The Schedule screen is the primary working screen of VTS3.

It allows the driver to:

view the daily schedule

manage trip status

track progress throughout the day

quickly access related actions (notes, lookup, phone, navigation)

The screen must remain fast, simple, and readable while driving or working.

Screen Layout Overview

The Schedule screen is divided into four vertical sections.

Date Navigation
Status Mode Selector
Status Counts
Trip List

The bottom navigation bar remains visible at all times.

Section 1 — Date Navigation Row

Purpose: Navigate between available schedule dates.

Layout:

Prev     Mon Mar 9, 2026     Next
Behavior

Prev:

loads the previous available schedule date

Next:

loads the next available schedule date

The date list is derived from the .xls files in the schedule folder.

Rules

Only available schedule dates are selectable

Navigation uses the availableDates list

The selected TripViewMode should remain unchanged when the date changes

Section 2 — Status Mode Selector

Purpose: Choose which bucket of trips to display.

Layout:

Active    Completed    Other
Modes

Active:

TripStatus.ACTIVE

Completed:

TripStatus.COMPLETED

Other:

REMOVED
CANCELLED
NOSHOW
Behavior

Selecting a mode filters the visible trip list.

Filtering does not reload data — it filters the existing list.

Section 3 — Status Counts

Purpose: Display running totals for the selected date.

Layout example:

Active: 4     Completed: 3     Other: 1
Count Rules

Active count:

status == ACTIVE

Completed count:

status == COMPLETED

Other count:

status == REMOVED
OR status == CANCELLED
OR status == NOSHOW
Important Requirement

Counts must update instantly when a trip status changes.

They must be derived from the same trip list used by the UI.

Section 4 — Trip List

The trip list displays the filtered schedule for the selected date.

Trips should be sorted by scheduled time.

The list scrolls vertically.

Trip Card Layout

Each trip is displayed as a card containing four rows.

Row 1 — Time + Passenger
Row 2 — Pickup Address
Row 3 — Dropoff Address
Row 4 — Actions
Row 1 — Time + Passenger

Example:

PA 13:40   Larry Colbert
Contents

appointment or pickup time

passenger name

Optional indicators may appear here later:

wheelchair

notes

etc.

Row 2 — Pickup Address

Example:

From:
123 Main St
Behavior

Later enhancement:

tapping address launches Waze navigation

Row 3 — Dropoff Address

Example:

To:
ABC Medical Center
Row 4 — Action Row

The actions available depend on the current trip status.

Active Trip Actions
Complete   No Show   Cancel   Remove
Notes   Phone   Lookup
Actions

Complete:

ACTIVE → COMPLETED

No Show:

ACTIVE → NOSHOW

Cancel:

ACTIVE → CANCELLED

Remove:

ACTIVE → REMOVED

Notes:

open trip notes editor (future feature)

Phone:

dial recognized clinic numbers (future feature)

Lookup:

open passenger history lookup

Completed Trip Actions

Completed trips only show:

Reinstate
Notes   Phone   Lookup
Reinstate Behavior
COMPLETED → ACTIVE

The completed overlay must be removed.

Other Trip Actions

Trips in Other display:

Reinstate
Status Label
Notes   Phone   Lookup

Example:

Reinstate    CANCEL

or

Reinstate    NO SHOW
Reinstate Behavior
REMOVED → ACTIVE
CANCELLED → ACTIVE
NOSHOW → ACTIVE

The corresponding overlay record must be removed.

Status Label Rules

Only trips in the Other view show a status label.

Labels are derived from TripStatus.

Examples:

REMOVED   → "REMOVED"
NOSHOW    → "NO SHOW"
CANCELLED → "CANCEL"

Completed trips do not display a status label.

List Behavior

The trip list must satisfy the following requirements.

Immediate updates

When status changes:

the card disappears from the current list if it no longer belongs

counts update immediately

no screen refresh required

Consistent sorting

Trips should always remain sorted by time.

Smooth scrolling

The list should support efficient scrolling with large schedules.

Empty State

If no trips exist for the selected filter:

Example:

No trips in this category
Loading State

When a new date is loading:

Possible indicators:

progress indicator

skeleton list

The UI must not freeze.

Error State

If schedule loading fails:

Display an error message and allow the user to retry.

Visual Style

The Schedule screen should maintain consistent VTS styling.

Guidelines:

green accent color (VtsGreen)

consistent card styling

readable typography

clear action buttons

minimal clutter

The UI must remain usable in bright environments and during active driving workflows.

Bottom Navigation

The bottom navigation bar remains visible at all times.

Destinations:

Schedule
Lookup
Drivers
Contacts
Clinics

The selected screen shows a soft green indicator bubble.

Summary

The Schedule screen provides a simple workflow:

Choose Date
Select Status Mode
View Trip List
Update Trip Status

All visible information must derive from a single unified schedule list produced by the schedule engine.

This ensures:

instant updates

consistent counts

reliable filtering

predictable reinstate behavior
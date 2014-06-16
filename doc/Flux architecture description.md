# Project Flux - Architecture Description

This project started out as a rather ambitious DevDay talk, and as such is full of cool ideas that present well, but will not necessarily be useful.

As this is a pet project being developed for the learning experience, some 'bad' decisions will be made in the design and requirements and parts of this system will be somewhat over-engineered, not necessarily useful, or purely done because we found it interesting. We are perfectly happy with this. Bad code however is unacceptable (We are trying to learn useful skills).

## Introduction

As software developers, we are always looking for new tools or processes to make our lives easier. This has led to Agile processes and Agile tools. Which unfortunately are often not very agile. We know that the solution is always to build more tools (Or create new processes)...

The idea is to build tools that take our existing tools and processes and improve them in interesting (and quite possibly useless) ways.

## Goals

* An easy way to print out JIRA tickets to use on a physical board.
* A mobile app that make JIRA easier to use in an agile team.
* An augmented view of a physical task board.
* An automated way to keep JIRA in sync with our physical board.

## Assumptions

* Nobody wants another issue tracker, so it makes sense to integrate with an existing one.

## Significant Requirements

* Print out coded tickets that can be recognised by the different systems.
* Connect to JIRA and display sprint details from a mobile app.
* Update ticket details on JIRA from a mobile app.
* Scan tickets from a mobile app.
* Identify and ID tickets on a realtime video feed.
* Recognise and id tickets from an image on sync app.

## Constraints

* As this is a pet project, time will be extremely constrained. 
* Must use JIRA as a issue tracker as the team has experience with JIRA and atlassian provides open source licenses.
* Must have a service layer allowing us to communicate with JIRA (and probably other API's)
* Mobile app will be developed on Android as the whole team is familiar with it and owns Android phones.
* The system needs to run on cheap hardware as each project/physical board will likely run it's own system (and we have a small budget).

## Architecture Style and Refinements

The system will use a *shared repository* architecture since it will use JIRA as a data store, with *layered* and *tiered* systems providing the necessary functionality.

![Architecture Overview](https://raw.githubusercontent.com/riaancornelius/Flux/master/doc/Overview.png)

## Mechanisms

<table>
<tr>
<th>Analysis</th>
<th>Design</th>
<th>Implementation</th>
<th>Reason</th>
</tr>
<tr>
<td>Communication</td>
<td>HTTP</td>
<td>JSON over REST</td>
<td>Small overhead, simple implementation. Existing libraries to do the work.</td>
</tr>
<tr>
<td rowspan=2>Messaging</td>
<td>Polling</td>
<td></td>
<td>App will need to check for updates from JIRA fairly often.</td>
</tr>
<tr>
<td>Push-to-sync</td>
<td>Google cloud messaging</td>
<td>Easy way to notify clients to sync. Minimises polling which extends battery life and reduces data cost on mobile apps.</td>
</tr>
</table>

## Team Overview

* **Riaan Cornelius** (Team Lead)
* **Elsabé Ros**
* **Martin Naude** (Business Owner)
* **Rishal Hurbens** (Tech Lead)

As this project was conceived as an Entelect DevDay talk, The work done for the talk can be treated as an architectural spike.

Most of the architectural decisions, requirements and constraints were uncovered during this architectural spike.

As Martin and Rishal only joined the team once this was rebranded as a Tech Accelerator project, their roles will be very limited. 
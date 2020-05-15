Getting Started
===============

These Instructions assume that you have a running version of registry ( https://github.com/susom/registry );

The folder structures in this repository are designed to align with those in registry.

This README is not a stand alone document, the more in-depth document is in the repository ( HL7_Messaging.docx );

The word document goes into:

    HL7 Messaging Changes
        Summary
            External References
        Compilation Changes
            ivy
            macker
        Database Changes
            ERD of the changes.
                EPIC_LOG Fields
        Import Changes
            Appointment
        Configuration Changes
            Log4j
            HL7 Destination
            Questionnaire Related Configuration
            Study Code Mapping
        Example Messages
        Code Changes
            HL7Sender
            HL7Generator
            EpicExportData
            EpicExportResult
            ReportUtils
            RegistryShortFormScoreProvider

This repository branch contains the code sections required to send the HL7 messages.
The Word document has sections to address configuration requirements, database changes, and summarizes the processes.

    .
    ├── HL7_Messaging.docx
    ├── HL7_Messaging_ERD.png
    ├── README.md
    └── src
        └── main
            ├── java
            │   └── edu
            │       ├── stanford
            │       │   └── registry
            │       │       └── server
            │       │           ├── survey
            │       │           │   └── RegistryShortFormScoreProvider.java
            │       │           └── utils
            │       │               └── ReportUtils.java
            │       └── ufl
            │           ├── registry
            │           │   ├── server
            │           │   │   ├── database
            │           │   │   │   └── objects
            │           │   │   │       └── EpicLogTable.java
            │           │   │   └── service
            │           │   │       └── hl7message
            │           │   │           ├── EpicExportData.java
            │           │   │           ├── EpicExportResult.java
            │           │   │           ├── HL7Generator.java
            │           │   │           └── HL7Transmission
            │           │   │               └── HL7Sender.java
            │           │   └── shared
            │           │       └── EpicLog.java
            │           └── survey
            │               └── server
            │                   └── SurveyCompleteHandlerHl7.java
            └── resources
                └── default
                    └── xchg
                        └── import_types
                            └── Appointment.xlsx
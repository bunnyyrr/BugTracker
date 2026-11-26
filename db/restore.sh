#!/bin/bash
psql -U bugtracker -d bugtracker < backup.sql

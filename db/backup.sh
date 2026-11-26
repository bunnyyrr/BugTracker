#!/bin/bash
pg_dump -U bugtracker -d bugtracker > backup.sql

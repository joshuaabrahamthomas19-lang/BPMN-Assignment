# Conceptual reference and authorship

Reference inspected: https://github.com/AlphinShabu/camunda/tree/main/pbj2-assignments

Inspected commit: `ec043cee113c6643dffeacb453b248aa00c2f3b6`.

The reference already targets Camunda 8. It establishes the three assignment concepts: lending, order fulfilment and hospital appointment triage. The supplied PBJ-2 question PDF establishes the required technologies and evidence.

All BPMN graphs, XML identifiers, rules, forms, Java source, mock contracts and documentation in this folder were newly authored for this implementation. No source files or compiled classes from the reference were copied. The reference has no root license file in the inspected checkout; this project uses the concepts rather than redistributing its implementation. Attribution is retained for transparency.

The implementation adds affordability-first lending, explicit commerce reservation release and distinct patient waitlist/decline outcomes. It is not described as a migration from Camunda 7 because the inspected source does not support that claim.

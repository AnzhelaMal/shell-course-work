package edu.semitotal.commander.server.events;

import org.springframework.data.repository.CrudRepository;

interface EventRecordRepository extends CrudRepository<ActionRecord, Long> {
}

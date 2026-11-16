package com.hms.hospital.service;

public interface AdminService {
    Object get(String resource, Long id);
    Object update(String resource, Long id, java.util.Map<String, Object> patch);
    void delete(String resource, Long id);
}

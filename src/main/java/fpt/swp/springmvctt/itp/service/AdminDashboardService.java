package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.dto.request.AdminDashboardRequest;
import fpt.swp.springmvctt.itp.dto.response.AdminDashboardResponse;

public interface AdminDashboardService {
    AdminDashboardResponse getDashboardData(AdminDashboardRequest request);
}

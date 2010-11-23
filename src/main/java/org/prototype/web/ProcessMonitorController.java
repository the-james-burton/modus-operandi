package org.prototype.web;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.prototype.monitor.ProcessMonitorService;
import org.prototype.web.ProcessLog.Line;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProcessMonitorController {

    private ProcessMonitorService services;
    private DateFormat dateFormatter;

    @Autowired
    public ProcessMonitorController(ProcessMonitorService services) {
        this.services = services;
        dateFormatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    }

    @RequestMapping(method = RequestMethod.GET, value = "/services.view")
    public String servicesGET(Model model) {
        model.addAttribute("pid", 0);
        updateTime(model);
        return "services";
    }

    @ModelAttribute("services")
    public ProcessMonitorService getServices() {
        return services;
    }

    @ModelAttribute("process")
    public Process populateProcess() {
        Process process = new Process();
        process.setPid(0);
        return process;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/services.view")
    public String servicesPOST(@ModelAttribute("process") Process process, Model model) {

        int pid = process.getPid();
        String windowTitle = process.getWindowTitle();
        if (pid == -1) {
            services.refresh();
        } else if (pid > 0) {
            services.killProcess(pid);
        } else if (windowTitle != null) {
            services.startProcess(Process.decodedWindowTitle(windowTitle));
        } else if (process.isStartAll()) {
            services.startAllProcesses();
        } else if (process.isStopAll()) {
            services.stopAllProcesses();
        }
        updateTime(model);
        return "services";
    }
    @RequestMapping(method = RequestMethod.POST, value = "/viewinfo.view")
    public String viewInfo(@RequestParam String windowTitle, Model model) {
        Process process = services.getProcess(Process.decodedWindowTitle(windowTitle));
        model.addAttribute("output", formatProperties(process.getProperties()));
        return "output";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/taillog.view")
    public String viewLogs(@RequestParam("windowTitle") String windowTitle, Model model) {
        windowTitle = Process.decodedWindowTitle(windowTitle);
        Process process = services.getProcess(windowTitle);
        model.addAttribute("output", "<pre>" + process.getLog().getTail() + "</pre>");
        return "output";
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/viewlogrange.view")
    public String viewLogsRange(@RequestParam("windowTitle") String windowTitle, @RequestParam("startLine") int start, @RequestParam("endLine") int end, Model model) {
        windowTitle = Process.decodedWindowTitle(windowTitle);
        Process process = services.getProcess(windowTitle);
        model.addAttribute("output", formatLines(process.getLog().getLineRange(start, end)));
        return "output";
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/viewlogtail.view")
    public String viewLogsTail(@RequestParam("windowTitle") String windowTitle, @RequestParam("lastLines") int lastLines, Model model) {
        windowTitle = Process.decodedWindowTitle(windowTitle);
        Process process = services.getProcess(windowTitle);
        model.addAttribute("output", formatLines(process.getLog().getLastLines(lastLines)));
        return "output";
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/viewlogfilter.view")
    public String viewLogsFilter(@RequestParam("windowTitle") String windowTitle, @RequestParam("filter") String filter, Model model) {
        windowTitle = Process.decodedWindowTitle(windowTitle);
        Process process = services.getProcess(windowTitle);
        model.addAttribute("output", formatLines(process.getLog().filterLines(filter)));
        return "output";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/viewlog.view")
    public String viewLogsFullPage(@RequestParam("windowTitle") String windowTitle, Model model) {
        windowTitle = Process.decodedWindowTitle(windowTitle);
        Process process = services.getProcess(windowTitle);
        model.addAttribute("output", process.getLog().getTail());
        return "taillog";
    }
    /**
     * Handles the login redirection produced by Spring Security.
     * @param success
     * @param model
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value="/login.view")
    public String login(@RequestParam(value = "success", required = false) String success, Model model) {
        if (success == null) {
            model.addAttribute("output", "first");
        } else {
            if (success.equals("true")) {
                model.addAttribute("output", "success");
            } else {
                model.addAttribute("output", "failure");
            }
        }
        return "login";
    }

//    @RequestMapping(method = RequestMethod.GET, value = "/cprocess.view")
//    public String cprocessOutput(Model model) {
//        model.addAttribute("cprocess", formatCProcessOutput(services.getCprocess()));
//        model.addAttribute("title", services.getMachine());
//        return "cprocess";
//    }

    @RequestMapping(value="/isloggedin.view")
    public String isLoggedIn(Model model) {
        boolean isLoggedIn = false;
        if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
            isLoggedIn = SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
        }
        model.addAttribute("output", String.valueOf(isLoggedIn));
        return "output";
    }

//    /**
//     * Formats output into an HTML table.
//     * 
//     * @param input
//     * @return
//     */
//    private String formatCProcessOutput(String input) {
//        String[] rows = input.split("\n");
//        StringBuilder sb = new StringBuilder("<table>\n<tbody>\n");
//        for (String row : rows) {
//            sb.append("\t<tr>\n");
//            String[] cells = row.split("\t");
//            for (String cell : cells) {
//                sb.append("\t\t<td>").append(cell).append("</td>\n");
//            }
//            sb.append("\t</tr>\n");
//        }
//        sb.append("</tbody>\n</table>");
//        return sb.toString();
//    }
    private String formatProperties(Map<String, String> props) {
        if (props == null) {
            return "No data available";
        }
        Iterator<String> iter = props.keySet().iterator();
        StringBuilder sb = new StringBuilder("<table>");
        
        while (iter.hasNext()) {
            String key = iter.next();
            sb.append("<tr><td>").append(key).append("</td><td>").append(props.get(key)).append("</td></tr>");
        }
        
        return sb.append("</table>").toString();
    }
    private String formatLines(List<Line> lines) {
        StringBuilder sb = new StringBuilder();
        if (lines == null || lines.size() == 0) {
            sb.append("No data");
        } else {
            for (Line line : lines) {
                sb.append("<div><span>")
                    .append(line.getLineNumber())
                    .append("</span>")
                    .append(line.getLine())
                    .append("</div>");
            }
        }
        
        return sb.toString();
    }
    private void updateTime(Model model) {
        String updateTime = dateFormatter.format(new Date());
        model.addAttribute("updateTime", updateTime);
        model.addAttribute("random", String.valueOf(Math.random()));
    }
}

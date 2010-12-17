package com.modusoperandi.web;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.modusoperandi.model.ConfigEntry;
import com.modusoperandi.model.Process;
import com.modusoperandi.model.ProcessLog.Line;
import com.modusoperandi.monitor.ProcessMonitorService;

@Controller
public class ProcessMonitorController {
    private static final Log            log = LogFactory.getLog(ProcessMonitorController.class);
    private final ProcessMonitorService services;
    private final DateFormat            dateFormatter;

    @Autowired
    public ProcessMonitorController(ProcessMonitorService services) {
        this.services = services;
        dateFormatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    }

    @RequestMapping(method = RequestMethod.GET, value = "/services.view")
    public String servicesGET(Model model, @RequestParam(required = false) boolean ajax) {
        model.addAttribute("pid", 0);
        updateTime(model);
        return ajax ? "services-ajax" : "services";
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
    public String servicesPOST(@ModelAttribute("process") Process process, @RequestParam(required = false) boolean startAll,
            @RequestParam(required = false) boolean stopAll, @RequestParam(required = false) boolean ajax, Model model) {
        int pid = process.getPid();
        String windowTitle = process.getWindowTitle();
        if (pid == -1) {
            services.refresh();
        } else if (pid > 0) {
            log.info(String.format("Received request to kill process %d.", pid));
            services.killProcess(pid);
        } else if (windowTitle != null) {
            services.startProcess(windowTitle);
        } else if (startAll) {
            services.startAllProcesses();
        } else if (stopAll) {
            services.stopAllProcesses();
        }
        updateTime(model);
        return ajax ? "services-ajax" : "services";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/viewinfo.view")
    public String viewInfo(@RequestParam String windowTitle, Model model) {
        Process process = services.getProcess(windowTitle);
        model.addAttribute("output", formatProperties(process.getProperties()));
        return "output";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/taillog.view")
    public String viewLogs(@RequestParam("windowTitle") String windowTitle, Model model) {
        Process process = services.getProcess(windowTitle);
        model.addAttribute("output", "<pre>" + process.getLog().getTail() + "</pre>");
        return "output";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/viewlogrange.view")
    public String viewLogsRange(@RequestParam("windowTitle") String windowTitle, @RequestParam("startLine") int start, @RequestParam("endLine") int end,
            Model model) {
        Process process = services.getProcess(windowTitle);
        model.addAttribute("output", formatLines(process.getLog().findLineRange(start, end)));
        return "output";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/viewlogtail.view")
    public String viewLogsTail(@RequestParam("windowTitle") String windowTitle, @RequestParam("lastLines") int lastLines, Model model) {
        Process process = services.getProcess(windowTitle);
        model.addAttribute("output", formatLines(process.getLog().findLastLines(lastLines)));
        return "output";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/viewlogfilter.view")
    public String viewLogsFilter(@RequestParam("windowTitle") String windowTitle, @RequestParam("filter") String filter, Model model) {
        Process process = services.getProcess(windowTitle);
        model.addAttribute("output", formatLines(process.getLog().filterLines(filter)));
        return "output";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/viewlog.view")
    public String viewLogsFullPage(@RequestParam("windowTitle") String windowTitle, Model model) {
        Process process = services.getProcess(windowTitle);
        model.addAttribute("output", process.getLog().getTail());
        return "taillog";
    }

    /**
     * Handles the login redirection produced by Spring Security.
     * 
     * @param success
     * @param model
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/login.view")
    public String login(@RequestParam(value = "success", required = false) String success, Model model, HttpServletResponse response) throws IOException {
        String view = "login";
        if (success == null) {
            model.addAttribute("output", "first");
        } else {
            if (success.equals("true")) {
                model.addAttribute("output", "success");
                view = "output";
            } else {
                model.addAttribute("output", "failure");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter();
                view = "output";
            }
        }
        return view;
    }

    @RequestMapping(value = "/isloggedin.view")
    public String isLoggedIn(Model model) {
        boolean isLoggedIn = false;
        if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
            isLoggedIn = SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
        }
        model.addAttribute("output", String.valueOf(isLoggedIn));
        return "output";
    }

    @RequestMapping(value = "/bulkInsert.view", method = RequestMethod.GET)
    public String bulkInsert() {
        return "bulkInsert";
    }

    @RequestMapping(value = "/bulkInsert.view", method = RequestMethod.POST)
    public String loadConfiguration(@RequestParam(value = "fileName") String fileName, HttpServletResponse response, Model model) {
        log.info(String.format("Loading config file '%s'.", fileName));
        try {
            File configFile = new File(fileName);
            if (!configFile.exists() || !configFile.canRead()) {
                throw new IllegalArgumentException(String.format("Config file %s cannot be accessed", fileName));
            }
            FileSystemResource lFileSystemResource = new FileSystemResource(configFile);
            BeanFactory factory = new XmlBeanFactory(lFileSystemResource);
            Object props = factory.getBean("properties");
            if (props != null) {
                if (!(props instanceof Collection<?>)) {
                    throw new IllegalArgumentException("properties bean defined in the config file isn't a Collection");
                }
                Collection<ConfigEntry> configs = new ArrayList<ConfigEntry>();
                Collection<?> configData = (Collection<?>) props;
                for (Object obj : configData) {
                    if (obj instanceof ConfigEntry) {
                        configs.add((ConfigEntry) obj);
                    }
                }
                log.info(String.format("Loaded %d ConfigEntry objects from %s.", configs.size(), fileName));
                services.removeAllConfigEntries();
                services.addAllConfigEntries(configs);
            }
            Object object = factory.getBean("bulkUpdate");
            if (object == null) {
                throw new IllegalArgumentException("Config file doesn't define the expected bulkUpdate list");
            }
            if (!(object instanceof Collection<?>)) {
                throw new IllegalArgumentException("bulkUpdate bean defined in the config file isn't a Collection");
            }
            Collection<Process> processes = new ArrayList<Process>();
            Collection<?> fileData = (Collection<?>) object;
            for (Object obj : fileData) {
                if (obj instanceof Process) {
                    processes.add((Process) obj);
                }
            }
            log.info(String.format("Loaded %d Process objects from %s.", processes.size(), fileName));
            services.removeAllProcesses();
            services.addAllProcesses(processes);
            services.refresh();
            // return environment name so it can be displayed
            model.addAttribute("output", services.getEnvironment());
        } catch (Throwable e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            model.addAttribute("output", e.getMessage());
            log.error("Bulk operation failed.", e);
        }
        return "output";
    }

    // TODO Remove!!!
    @RequestMapping(value = "/deleteall.view", method = RequestMethod.GET)
    public String deleteAll() {
        services.removeAllProcesses();
        services.removeAllConfigEntries();
        return "output";
    }

    // /**
    // * Formats output into an HTML table.
    // *
    // * @param input
    // * @return
    // */
    // private String formatCProcessOutput(String input) {
    // String[] rows = input.split("\n");
    // StringBuilder sb = new StringBuilder("<table>\n<tbody>\n");
    // for (String row : rows) {
    // sb.append("\t<tr>\n");
    // String[] cells = row.split("\t");
    // for (String cell : cells) {
    // sb.append("\t\t<td>").append(cell).append("</td>\n");
    // }
    // sb.append("\t</tr>\n");
    // }
    // sb.append("</tbody>\n</table>");
    // return sb.toString();
    // }
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
                sb.append("<div><span>").append(line.getLineNumber()).append("</span>").append(line.getLine()).append("</div>");
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

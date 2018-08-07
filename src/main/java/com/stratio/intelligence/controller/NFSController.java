package com.stratio.intelligence.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.jcraft.jsch.*;
import com.stratio.intelligence.dto.VolumeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@RestController
@Validated
public class NFSController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final String ROOT_PATH = "/var/sds/intelligence/";

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity<String> createInstanceUserFolder( @RequestBody(required=true) VolumeConfiguration volumeConfiguration) {

        try{
            String username = volumeConfiguration.getOwner();
            String usernameGroup = volumeConfiguration.getOwnerGroup();
            String userWorkspaceFolder = volumeConfiguration.getInstance() + "/analytic/users/" + username + "/workspace";

            String commandCheckUser = "id " + username;
            String commandCreateUser = "useradd -g " + usernameGroup + " " + username;
            String commandCreateFolder = "mkdir -p " + ROOT_PATH + userWorkspaceFolder;
            String commandSetPermissions = "chmod 775 " + ROOT_PATH + userWorkspaceFolder;
            String commandSetOwnership = "chown " + username + " " + ROOT_PATH + userWorkspaceFolder;
            String host = "paas-nfs.labs.stratio.com";
            String user = "root";
            Integer port = 22;
            String password = "stratio";

            // init configuration of connection session
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);;
            session.setPassword(password);
            session.connect();

            // set commands

            log.info(" -> - checking user");
            log.info(" -> command: " + commandCheckUser);
            log.info(" -> - exit status: " + this.executeCommand(session, commandCheckUser));


            log.info(" -> - creating user");
            log.info(" -> command: " + commandCreateUser);
            log.info(" -> - exit status: " + this.executeCommand(session, commandCreateUser));


            log.info(" -> - creating folder");
            log.info(" -> command: " + commandCreateFolder);
            log.info(" -> - exit status: " + this.executeCommand(session, commandCreateFolder));


            log.info(" -> - set permissions");
            log.info(" -> command: " + commandSetPermissions);
            log.info(" -> - exit status: " + this.executeCommand(session, commandSetPermissions));


            log.info(" -> - set ownership");
            log.info(" -> command: " + commandSetOwnership);
            log.info(" -> - exit status: " + this.executeCommand(session, commandSetOwnership));


            session.disconnect();
        }catch(Exception ex){
            ex.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    private int executeCommand(Session session, String command) throws JSchException, IOException {
        int commandExitStatus = -1;
        Channel channel = session.openChannel("exec");
        ((ChannelExec)channel).setCommand(command);

        channel.setInputStream(null);
        ((ChannelExec)channel).setErrStream(System.err);
        InputStream input = channel.getInputStream();
        channel.connect();

        // commandExitStatus = channel.getExitStatus();
        try{
            InputStreamReader inputReader = new InputStreamReader(input);
            BufferedReader bufferedReader = new BufferedReader(inputReader);
            String line = null;
            while((line = bufferedReader.readLine()) != null){
                System.out.println(line);
            }
            bufferedReader.close();
            inputReader.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        // TODO controlar channel.getExitStatus(), sólo se actualiza cuando channel.isClosed() == true
        commandExitStatus = channel.getExitStatus();
        channel.disconnect();

        return commandExitStatus;
    }
}

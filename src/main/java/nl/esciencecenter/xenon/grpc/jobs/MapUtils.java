package nl.esciencecenter.xenon.grpc.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import nl.esciencecenter.xenon.AdaptorStatus;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.grpc.XenonProto;
import nl.esciencecenter.xenon.jobs.*;

import static nl.esciencecenter.xenon.grpc.MapUtils.mapPropertyDescriptions;

class MapUtils {
    private MapUtils() {
    }

    static XenonProto.QueueStatus mapQueueStatus(QueueStatus status, XenonProto.Scheduler scheduler) {
        XenonProto.QueueStatus.Builder builder = XenonProto.QueueStatus.newBuilder()
                .setName(status.getQueueName())
                .setScheduler(scheduler)
                .putAllSchedulerSpecificInformation(status.getSchedulerSpecficInformation());
        if (status.hasException()) {
            builder.setError(status.getException().getMessage());
        }
        return builder.build();
    }

    static String defaultValue(String value) {
        if (value.isEmpty()) {
            return null;
        }
        return value;
    }

    static JobDescription mapJobDescription(XenonProto.SubmitJobRequest request) {
        XenonProto.JobDescription d = request.getDescription();
        JobDescription description = new JobDescription();
        description.setExecutable(d.getExecutable());
        description.setArguments(d.getArgumentsList().toArray(new String[0]));
        description.setWorkingDirectory(d.getWorkingDirectory());
        description.setEnvironment(d.getEnvironmentMap());
        description.setQueueName(defaultValue(d.getQueueName()));
        description.setInteractive(d.getInteractive());
        if (d.getMaxTime() != 0) {
            description.setMaxTime(d.getMaxTime());
        }
        if (d.getNodeCount() != 0) {
            description.setNodeCount(d.getNodeCount());
        }
        if (d.getProcessesPerNode() != 0) {
            description.setProcessesPerNode(d.getProcessesPerNode());
        }
        description.setStartSingleProcess(d.getStartSingleProcess());
        description.setStderr(defaultValue(d.getStdErr()));
        description.setStdin(defaultValue(d.getStdIn()));
        description.setStdout(defaultValue(d.getStdOut()));
        description.setJobOptions(d.getOptionsMap());
        return description;
    }

    static XenonProto.JobDescription mapJobDescription(Job job, XenonProto.Scheduler scheduler) {
        XenonProto.JobDescription.Builder builder = XenonProto.JobDescription.newBuilder()
                .setScheduler(scheduler);
        JobDescription description = job.getJobDescription();
        if (description != null) {
            builder
                    .setExecutable(description.getExecutable())
                    .addAllArguments(description.getArguments())
                    .setWorkingDirectory(description.getWorkingDirectory())
                    .putAllEnvironment(description.getEnvironment())
                    .setQueueName(description.getQueueName())
                    .setInteractive(description.isInteractive())
                    .setMaxTime(description.getMaxTime())
                    .setNodeCount(description.getNodeCount())
                    .setProcessesPerNode(description.getProcessesPerNode())
                    .setStartSingleProcess(description.isStartSingleProcess())
                    .setStdErr(description.getStderr())
                    .setStdIn(description.getStdin())
                    .setStdOut(description.getStdout())
                    .putAllOptions(description.getJobOptions())
            ;
        }
        return builder.build();
    }

    static XenonProto.JobStatus mapJobStatus(JobStatus status, XenonProto.JobDescription description) {
        XenonProto.JobStatus.Builder builder = XenonProto.JobStatus.newBuilder()
                .setState(status.getState())
                .setRunning(status.isRunning())
                .setDone(status.isDone())
                .putAllSchedulerSpecificInformation(status.getSchedulerSpecficInformation())
                .setJob(XenonProto.Job.newBuilder()
                        .setId(status.getJob().getIdentifier())
                        .setDescription(description)
                        .build())
                .setExitCode(status.getExitCode());
        if (status.hasException()) {
            builder
                    .setErrorMessage(status.getException().getMessage())
                    .setErrorType(mapErrorType(status.getException()));
        }

        return builder.build();
    }

    private static XenonProto.JobStatus.ErrorType mapErrorType(Exception exception) {
        if (exception instanceof JobCanceledException) {
            return XenonProto.JobStatus.ErrorType.CANCELLED;
        } else if (exception instanceof NoSuchJobException) {
            return XenonProto.JobStatus.ErrorType.NOT_FOUND;
        } else if (exception instanceof NoSuchSchedulerException) {
            return XenonProto.JobStatus.ErrorType.SCHEDULER_NOT_FOUND;
        }
        // map other (Xenon) exceptions to own type
        return XenonProto.JobStatus.ErrorType.OTHER;
    }

    static XenonProto.Job mapJob(String key, XenonProto.JobDescription description) {
        return XenonProto.Job.newBuilder().setId(key).setDescription(description).build();
    }

    static XenonProto.JobAdaptorDescription mapJobAdaptorDescription(AdaptorStatus status) {
        List<XenonProto.PropertyDescription> supportedProperties = mapPropertyDescriptions(status);
        return XenonProto.JobAdaptorDescription.newBuilder()
                .setName(status.getName())
                .setDescription(status.getDescription())
                .addAllSupportedLocations(Arrays.asList(status.getSupportedLocations()))
                .addAllSupportedProperties(supportedProperties)
                .build();
    }
}
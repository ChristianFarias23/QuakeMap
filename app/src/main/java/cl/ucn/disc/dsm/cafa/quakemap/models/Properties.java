package cl.ucn.disc.dsm.cafa.quakemap.models;

import lombok.Getter;

/**
 * Las propiedades del terremoto.
 */
public class Properties  {

    @Getter
    private Float mag;

    @Getter
    private String place;

    @Getter
    private Long time;

    @Getter
    private Integer updated;

    @Getter
    private Integer tz;

    @Getter
    private String url;

    @Getter
    private String detail;

    @Getter
    private String felt;

    @Getter
    private String cdi;

    @Getter
    private String mmi;

    //“green”, “yellow”, “orange”, “red”.

    @Getter
    private String alert;

    @Getter
    private String status;

    @Getter
    private Integer tsunami;

    @Getter
    private Integer sig;

    @Getter
    private String net;

    @Getter
    private String code;

    @Getter
    private String ids;

    @Getter
    private String sources;

    @Getter
    private String types;

    @Getter
    private Integer nst;

    @Getter
    private Float dmin;

    @Getter
    private Float rms;

    @Getter
    private Integer gap;

    @Getter
    private String magType;

    @Getter
    private String type;

    @Getter
    private String title;
}

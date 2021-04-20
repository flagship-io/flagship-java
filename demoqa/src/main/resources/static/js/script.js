var app = new Vue({
    el: "#app",
    data: {
        envId: "",
        apiKey: "",
        timeout: 2000,
        pollingInterval: 60000,
        bucketing: true,
        visitorId: "test-visitor",
        context: "{\n}",
        envOk: false,
        envError: null,
        visitorOk: false,
        visitorError: null,
        eventOk: false,
        eventError: null,
        data: null,
        hit: { t: "EVENT" },
        hitTypes: ["EVENT", "TRANSACTION", "ITEM", "PAGE", "SCREEN"],
        flag: { name: "", type: "bool", defaultValue: "", activate: true },
        flagOk: false,
        flagUpdateContextOk: false,
        flagModificationOk: false,
        flagInfo: { name: "" },
        flagModification: { name: "" },
        flagUpdateContext: { name: "", type: "bool", value: "" },
        flagInfoOk: false,
    },
    methods: {
        getEnv() {
            this.$http.get("/env").then((response) => {
                // get body data
                this.currentEnv = response.body;
                this.bucketing = response.body.bucketing;
                this.envId = response.body.environment_id;
                this.apiKey = response.body.api_key;
                this.timeout = response.body.timeout;
                this.pollingInterval = response.body.polling_interval;
            });
        },
        setEnv() {
            this.envOk = false;
            this.envError = null;
            this.$http
                .put("/env", {
                    environment_id: this.envId,
                    api_key: this.apiKey,
                    bucketing: this.bucketing,
                    timeout: this.timeout || 0,
                    polling_interval: this.pollingInterval || 0,
                })
                .then(
                    (response) => {
                        this.envOk = true;
                    },
                    (response) => {
                        this.envOk = false;
                        this.envError = response.body;
                    }
                );
        },
        getVisitor() {
            this.$http.get("/visitor").then((response) => {
                // get body data
                this.visitorId = response.body.visitor_id;
                this.context = JSON.stringify(response.body.context);
                
            });
        },
        setVisitor() {
            this.visitorOk = false;
            this.visitorError = null;
            this.data = null;

            this.$http
                .put("/visitor", {
                    visitor_id: this.visitorId,
                    context: this.context ? JSON.parse(this.context) : "{}",
                })
                .then(
                    (response) => {
                        // get body data
                        this.data = response.body;
                        this.visitorOk = true;
                    },
                    (response) => {
                        this.visitorOk = false;
                        this.visitorError = response.body;
                    }
                );
        },
        changeType(e) {
            this.hit = {
                t: this.hit.t,
            };
        },
        sendHit() {
            this.eventOk = false;
            this.eventError = null;

            this.$http.post("/hit", this.hit).then(
                () => {
                    this.eventOk = true;
                },
                (response) => {
                    this.eventOk = false;
                    this.eventError = response.body;
                }
            );
        },
        getFlag() {
            this.flagOk = false;

            const { name, type, activate, defaultValue } = this.flag;

            if (!name || !type) {
                this.flagOk = { err: "Missing flag name or type" };
                return;
            }
            console.log(this.flag);

            this.$http
                .get(
                    `/flag/${name}`, 
                    {
                        params:
                        {
                            type,
                            activate,
                            defaultValue
                        }
                    }
                )
                .then(
                    (response) => {
                        this.flagOk = response.body;
                    },
                    (response) => {
                        this.flagOk = response.body;
                    }
                );
        },
        getFlagInfo() {
            this.flagInfoOk = false;

            const { name } = this.flagInfo;

            if (!name) {
                this.flagInfoOk = { err: "Missing flag name or type" };
                return;
            }

            this.$http.get(
                `/flag/${name}/info`
            ).then(
                (response) => {
                    this.flagInfoOk = response.body.value;
                },
                (response) => {
                       this.flagInfoOk = response.body;
                }
            );
        },
        getUpdateContext() {
            this.flagUpdateContextOk = false;
            this.data = null;

            const { name, type, value } = this.flagUpdateContext;

            if (!name || !type) {
                this.flagUpdateContextOk = { err: "Missing flag name or type" };
                return;
            }
            console.log(this.flagUpdateContext);

            this.$http
                .get(
                    `/flag/${name}/updateContext`,
                    {
                        params:
                        {
                            type,
                            value
                        }
                    }
                )
                .then(
                    (response) => {
                        this.data = response.body
                    },
                    (response) => {
                        this.data = response.body
                    }
                );
        },
        getModification() {
                    this.flagModificationOk = false;

                    const { name } = this.flagModification;

                    if (!name ) {
                        this.flagModificationOk = { err: "Missing flag name or type" };
                        return;
                    }
                    console.log(this.flagModification);

                    this.$http
                        .get(`/flag/${name}/activate`)
                        .then(
                            (response) => {
                                this.flagModificationOk = response.body;
                            },
                            (response) => {
                                this.flagModificationOk = response.body;
                            }
                        );
        },
        getLogs() {
            this.$http
                .get(`/logs`)
                .then(
                    (response) => {
                        this.data = response.bodyText;
                    },
                    (response) => {
                        this.data = response.bodyText;
                    }
                );
        },
        clearLogs() {
            this.$http
                .get(`/clear`)
                .then(
                    (response) => {
                        this.data = response.bodyText;
                    },
                    (response) => {
                        this.data = response.bodyText;
                    }
                );
        }
    },
    mounted() {
        this.getEnv();
        this.getVisitor();
        this.getLogs();
    },
});

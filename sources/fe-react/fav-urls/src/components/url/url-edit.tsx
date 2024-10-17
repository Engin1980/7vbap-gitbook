import {SubmitHandler, useForm} from "react-hook-form";
import {MDBBtn, MDBCol, MDBContainer, MDBInput, MDBRow} from "mdb-react-ui-kit";
import axios from "axios";
import {toast} from "react-toastify";

type Data = {
  address : string;
  title : string;
  appUserId : number;
};

function UrlEdit(){
  const {
    register,
    handleSubmit,
    formState : {errors}
  } = useForm<Data>();

  const submitHandler : SubmitHandler<Data> = async data =>{
    const appUserId = 1; // mock appUserId of an existing user

    const formData = new FormData();
    formData.append("address", data.address);
    formData.append("title", data.title);
    formData.append("appUserId", appUserId.toString());

    try{
      await axios.post("http://localhost:32123/v1/url", formData);
      toast.success("Link stored successfully.");
    }catch (err){
      console.log(err);
      toast.error("Link stored failed.");
    }
  }

  return (<div>
    <MDBContainer>
      <MDBRow className="justify-content-center">
        <MDBCol md="6">
          <form onSubmit={handleSubmit(submitHandler)}>

            <MDBInput
              className="mb-3 mt-4"
              label="Title"
              type="text"
              {...register("title", {
                required: "This field is required."
              })}
            />
            {errors.title && (
              <span className="invalid-feedback d-block">
                        {errors.title.message}
                      </span>
            )}

            <MDBInput
              className="mb-3 mt-4"
              label="Address"
              type="url"
              {...register("address", {
                required: "This field is required",
                pattern: {
                  value: /^http.+\..+/,
                  message: "URL Address must start with http...",
                },
              })} />
            {errors.address && (
              <span className="invalid-feedback d-block">
                      {errors.address.message}
                    </span>
            )}

            <MDBBtn type="submit">Save</MDBBtn>
          </form>
        </MDBCol>
      </MDBRow>
    </MDBContainer>
  </div>);
}

export default UrlEdit;
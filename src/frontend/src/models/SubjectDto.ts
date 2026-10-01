export default class SubjectDto {
  id?: number
  name?: string
  code?: string
  active?: boolean

  constructor(obj?: Partial<SubjectDto>) {
    Object.assign(this, obj)
  }
}
